package com.fittness.aiservice;

import com.fittness.aiservice.DTO.Activity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAIService {
    //get activity generate recommendation according to the activiry and user

     private final GeminiService geminiService;
     private final repository repo;
     public recommendation generateRecommendation(Activity activity) throws Exception {
         String prompt=createPromptForActivity(activity);
         String aiResponse=geminiService.getAnswer(prompt);
         log.info("Response from AI:", aiResponse);
       return  processeAiResponse(aiResponse,activity);

     }

    private String createPromptForActivity(Activity activity) {
        return String.format("""
Analyze this fitness activity and provide detailed recommendations.

Return ONLY valid JSON with this exact structure:

{
  "analysis": {
    "overall": "Overall analysis here",
    "pace": "Pace analysis here",
    "heartRate": "Heart rate analysis here",
    "caloriesBurned": "Calories analysis here"
  },
  "improvements": [
    {
      "area": "Area name",
      "recommendation": "Detailed recommendation"
    }
  ],
  "suggestions": [
    {
      "workout": "Workout name",
      "description": "Detailed workout description"
    }
  ],
  "safety": [
    "Safety point 1",
    "Safety point 2"
  ]
}
""",
                activity.getDuration(),
                activity.getType(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics());
     }




    private recommendation processeAiResponse(String airesponse,Activity activity) throws Exception{

        try {

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(airesponse);

            JsonNode textNode = rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");

            String jsonContent = textNode.asText()
                    .replaceAll("```json\\n", "")
                    .replaceAll("\\n```", "")
                    .trim();

            log.info("parsed Response from Ai: {}", jsonContent);

            JsonNode analysisJson = mapper.readTree(jsonContent);

            JsonNode analysisNode = analysisJson.path("analysis");

            StringBuilder fullAnalysis = new StringBuilder();
            addAnalysisSection(fullAnalysis, analysisNode, "overall", "OverAll:");
            addAnalysisSection(fullAnalysis, analysisNode, "heartRate", "heartRate:");
            addAnalysisSection(fullAnalysis, analysisNode, "pace", "Pace:");
            addAnalysisSection(fullAnalysis, analysisNode, "caloriesBurned", "calories Burned:");

            List<String> improvements = extractImprovements(analysisJson.path("improvements"));
            List<String> suggestions = extractSuggestions(analysisJson.path("suggestions"));
            List<String> safety = extractSafety(analysisJson.path("safety"));

              recommendation recc=recommendation.builder().suggestions(suggestions)
                    .improvements(improvements)
                    .safety(safety)
                    .userId(activity.getUserId())
                    .activityId(activity.getId())
                    .activityType(activity.getType())
                    .improvements(Collections.singletonList(fullAnalysis.toString().trim()))
                    .createdAt(LocalDateTime.now()).build();
            repo.save(recc);
            return recc;
   } catch (Exception e) {
            log.error("Error processing AI response", e);
            return createDefaultRecommendation( activity);

        }
    }

    private recommendation createDefaultRecommendation(Activity activity) {
         return recommendation.builder().
                 activityId(activity.getId())
                         .recommendation(activity.getUserId())
                                 .activityType(activity.getType())
                                         .improvements(Collections.singletonList("improvements"))
                                                 .suggestions(Collections.singletonList("suggestions"))
                                                         .safety(Arrays.asList("safety1","safety2"))
                 .createdAt(LocalDateTime.now())
                 .build();
    }


    private List<String> extractSafety(JsonNode safetyNode) {
        List<String> safetyl=new ArrayList<>();
        if(safetyNode.isArray()){
            safetyNode.forEach(item->safetyl.add(item.asText()));


        }
        return safetyl.isEmpty()?
                Collections.singletonList("Follow general safety guides"):
                safetyl;
    }

    private List<String> extractSuggestions(JsonNode suggestionsNode) {
        List<String> suggestions=new ArrayList<>();
        if(suggestionsNode.isArray()){
            suggestionsNode.forEach(suggestion->{
                String workout=suggestion.path("workout").asText();
                String description=suggestion.path("description").asText();
                suggestions.add(String.format("%s: %s",workout, description));
            });

        }

        return suggestions.isEmpty()?
                Collections.singletonList("No specefic suggestion provides"):
                suggestions;
    }

    private List<String> extractImprovements(JsonNode improvementsNode) {
        List<String> improvments=new ArrayList<>();
        if(improvementsNode.isArray()){
            improvementsNode.forEach(improvement->{
                String area=improvement.path("area").asText();
                String recommendation=improvement.path("recommendation").asText();
                improvments.add(String.format("%s: %s",area, recommendation));
            });

        }
        return improvments.isEmpty()?
                Collections.singletonList("No specefic improvements provides"):
                improvments;
    }

    private void addAnalysisSection(StringBuilder fullAnalysis, JsonNode analysisNode, String key, String prefix) {

        if(!analysisNode.path(key).isMissingNode()){
            fullAnalysis.append(prefix).append(analysisNode.path(key).asText()).append("\n\n");
        }
    }


}
