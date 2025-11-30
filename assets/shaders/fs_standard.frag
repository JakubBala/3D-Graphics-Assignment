#version 330 core

in vec3 aPos;
in vec3 aNormal;
in vec2 aTexCoord;

out vec4 fragColor;

uniform vec3 viewPos;

#define MAX_LIGHTS 8

struct Light {
    vec3 position;
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    
    int type;  // 0=directional, 1=point, 2=spot
    
    // Attenuation (for point & spot lights)
    float constant;
    float linear;
    float quadratic;
    
    // Spotlight
    vec3 direction;
    float cutOff;
    float outerCutOff;
};

uniform Light lights[MAX_LIGHTS];
uniform int numActiveLights;

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

// Calculate lighting for one light source
vec3 calculateLight(Light light, vec3 normal, vec3 fragPos, vec3 viewDir) {
    vec3 lightDir;
    float attenuation = 1.0;
    
    // --- Calculate light direction and attenuation ---
    if (light.type == 0) {
        // Directional light
        lightDir = normalize(-light.direction);
    } else {
        // Point or Spot light
        lightDir = normalize(light.position - fragPos);
        
        // Attenuation
        float distance = length(light.position - fragPos);
        attenuation = 1.0 / (light.constant + light.linear * distance + 
                            light.quadratic * (distance * distance));
        
        // Spotlight intensity
        if (light.type == 2) {
            float angle = dot(lightDir, normalize(-light.direction));
            float cutoffRange = light.cutOff - light.outerCutOff;
            float intensity = clamp((angle - light.outerCutOff) / cutoffRange, 0.0, 1.0);
            attenuation *= intensity;
        }
    }
    
    // --- Ambient ---
    vec3 ambient = light.ambient * material.ambient;
    
    // --- Diffuse ---
    float diff = max(dot(normal, lightDir), 0.0);
    vec3 baseColor = material.diffuse;
    if (material.hasAlbedoMap == 1) {
        baseColor = texture(material.albedoMap, aTexCoord).rgb;
    }
    vec3 diffuse = light.diffuse * diff * baseColor;
    
    // --- Specular ---
    // Blinn-Phong reflection: uses half-vector
    vec3 halfDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfDir), 0.0), material.shininess);

    // Choose either constant spec color or from specular map
    vec3 specColor = material.specular;
    if (material.hasSpecularMap == 1) {
        specColor = texture(material.specularMap, aTexCoord).rgb;
    }

    vec3 specular = light.specular * specColor * spec;
    
    // Apply attenuation
    diffuse *= attenuation;
    specular *= attenuation;
    
    return (ambient + diffuse + specular);
}

void main() {
    vec3 norm = normalize(aNormal);
    vec3 viewDir = normalize(viewPos - aPos);
    
    // Accumulate lighting from all lights
    vec3 result = vec3(0.0);
    for (int i = 0; i < numActiveLights; i++) {
        result += calculateLight(lights[i], norm, aPos, viewDir);
    }
    
    // --- Emission (not affected by lights) ---
    if (material.hasEmissionMap == 1) {
        result += texture(material.emissionMap, aTexCoord).rgb;
    }
    
    fragColor = vec4(result, 1.0);
}