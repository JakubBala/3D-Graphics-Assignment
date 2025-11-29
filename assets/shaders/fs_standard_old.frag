#version 330 core

in vec3 aPos;
in vec3 aNormal;
in vec2 aTexCoord;

out vec4 fragColor;

uniform vec3 viewPos;

struct Light {
  vec3 position;
  vec3 ambient;
  vec3 diffuse;
  vec3 specular;
};

uniform Light light;

struct Material {
  vec3 ambient;
  vec3 diffuse;
  vec3 specular;
  float shininess;

  sampler2D albedoMap;
  sampler2D specularMap;
  sampler2D emissionMap;

  int hasAlbedoMap;
  int hasSpecularMap;
  int hasEmissionMap;
};

uniform Material material;

void main() {
    vec3 norm = normalize(aNormal);
    vec3 lightDir = normalize(light.position - aPos);

    // --- Ambient ---
    vec3 ambient = light.ambient * material.ambient;

    // --- Diffuse ---
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 baseColor = material.diffuse;
    baseColor *= material.hasAlbedoMap * texture(material.albedoMap, aTexCoord).rgb;

    vec3 diffuse = light.diffuse * diff * baseColor;

    // --- Specular ---
    vec3 viewDir = normalize(viewPos - aPos);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);

    vec3 specColor = material.specular;
    specColor *= material.hasSpecularMap * texture(material.specularMap, aTexCoord).rgb;

    vec3 specular = light.specular * spec * specColor;

    // --- Emission ---
    vec3 emission = vec3(0.0);
    emission = material.hasEmissionMap * texture(material.emissionMap, aTexCoord).rgb;

    // --- Final Output ---
    vec3 result = ambient + diffuse + specular + emission;
    fragColor = vec4(result, 1.0);
}