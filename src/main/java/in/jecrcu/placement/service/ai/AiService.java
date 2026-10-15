package in.jecrcu.placement.service.ai;

public interface AiService {
    boolean isAvailable();
    String summarizeCompanyExperience(String companyName, String aggregatedExperiences);
    String classifyQuestion(String questionText);
    String answerPrepQuestion(String userQuery, String relevantContext);
}
