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
    float intensity;
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

  vec2 tiling;
};

uniform Material material;

vec3 ApplyLight(
    Light light,
    vec3 normal,
    vec3 fragPos,
    vec3 viewDir,
    vec3 ambientBase,
    vec3 diffuseBase,
    vec3 specularBase
) {
    vec3 lightDir;
    float attenuation = 1.0;

    if (light.type == 0) {
        // Directional light
        lightDir = normalize(-light.direction);
    }
    else {
        // Point or spot
        vec3 L = light.position - fragPos;
        float distance = length(L);
        lightDir = L / distance;

        attenuation = 1.0 / (light.constant +
                             light.linear * distance +
                             light.quadratic * distance * distance);

        if (light.type == 2) {
            // Spotlight intensity
            float theta = dot(lightDir, normalize(-light.direction));
            float epsilon = light.cutOff - light.outerCutOff;
            float intensity = clamp((theta - light.outerCutOff) / epsilon, 0.0, 1.0);
            attenuation *= intensity;
        }
    }

    // Ambient
    vec3 ambient = light.ambient * ambientBase;

    // Diffuse
    float diff = max(dot(normal, lightDir), 0.0);
    vec3 diffuse = light.diffuse * diff * diffuseBase;

    // Blinn-Phong specular
    vec3 halfDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfDir), 0.0), material.shininess);
    vec3 specular = light.specular * spec * specularBase;

    ambient *= attenuation; 
    diffuse *= attenuation;
    specular *= attenuation;

    // Apply per-light intensity (makes directional lights scale correctly)
    return (ambient + diffuse + specular) * light.intensity;
}

void main() {
    vec3 normal = normalize(aNormal);
    vec3 viewDir = normalize(viewPos - aPos);

    float alpha = 1.0;

    vec2 uv = aTexCoord * material.tiling;

    // --- BASE MATERIAL VALUES ---
    vec3 ambientBase  = material.ambient;
    vec3 diffuseBase  = material.diffuse;
    vec3 specularBase = material.specular;

    if (material.hasAlbedoMap == 1) {
        vec4 tex = texture(material.albedoMap, uv);
        ambientBase *= tex.rgb;
        diffuseBase *= tex.rgb;
        alpha = tex.a;
    }

    if(alpha < 0.1) {
        discard;
    }

    if (material.hasSpecularMap == 1) {
        specularBase *= texture(material.specularMap, uv).rgb;
    }

    // --- LIGHT ACCUMULATION ---
    vec3 color = vec3(0.0);

    for (int i = 0; i < numActiveLights; i++) {
        color += ApplyLight(
            lights[i],
            normal,
            aPos,
            viewDir,
            ambientBase,
            diffuseBase,
            specularBase
        );
    }

    // --- EMISSION DOES NOT RECEIVE LIGHT ---
    if (material.hasEmissionMap == 1) {
        color += texture(material.emissionMap, uv).rgb;
    }

    fragColor = vec4(color, alpha);
}