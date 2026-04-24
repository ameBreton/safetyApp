package com.example.project666;

import java.util.List;

/**
 * This class responsible for generating dynamic safety tips based on user responses.
 * <p>
 * This class takes a {@link Question} and a list of all questions, and produces a personalized tip
 * string by selecting appropriate suggestions and replacing placeholders like <code>{}</code>,
 * <code>{safe_room}</code>, and <code>{abuse_type}</code> with the user's answers.
 * <p>
 * This logic helps generates safety advice to the user's unique situation.
 *
 * @author Dylan Chen
 *
 * @version 1.0
 */
public class TipProducer {

    /**
     * Generates a personalized tip for a given question based on the user's answer.
     *
     * @param question The current question to generate a tip for
     * @param questions The list of all questions, used for context substitution (e.g., safe room info)
     * @return A tip string with personalized content
     */
    public static String generateTip(Question question, List<Question> questions) {
        StringBuilder tip = new StringBuilder();
        tip.append("‣  "); //Tip starts with ‣


        if (question.getTips().size() == 1) {
            //Only one tip available
            tip.append(question.getTips().get(0));
        } else {
            if (question.getOptions().contains(question.getUserAnswer())) {
                //Except Sintext question, other question have same answer with options
                tip.append(question.getTips().get(question.getOptions().indexOf(question.getUserAnswer())));
            }
            else {
                //Sintext question with answer yes and further text answer
                tip.append(question.getTips().get((0)));
            }
        }

        // Replacee placehoders with answers
        return TipProducer.replaceBrack(tip, question, questions).toString();
    }

    /**
     * Replaces placeholders in the tip with actual user answers.
     * Supported placeholders:
     * <ul>
     *   <li><code>{}</code> → replaced with current question's userAnswer</li>
     *   <li><code>{safe_room}</code> → replaced with the answer to the third question</li>
     *   <li><code>{abuse_type}</code> → replaced with 'multiple' or first selected abuse type</li>
     * </ul>
     *
     * @param tip The StringBuilder containing the tip text
     * @param question The current question being processed
     * @param questions The full question list, used to retrieve other answers
     * @return A StringBuilder with all placeholders replaced
     */
    public static StringBuilder replaceBrack(StringBuilder tip, Question question, List<Question> questions){
        int index = tip.indexOf("{}");
        if (index != -1) {
            //Ordinary placehoders case
            tip.replace(index, index+2, question.getUserAnswer());
            return tip;
        }
        index = tip.indexOf("{safe_room}");
        if (index != -1) {
            //Question 3 is about safe room
            tip.replace(index, index+11, questions.get(2).getUserAnswer());
            return tip;
        }
        index = tip.indexOf("{abuse_type}");
        if (index != -1) {
            if (question.getUserAnswers().size()>1) {
                // More than one abuse type chosen
                tip.replace(index, index+12, "multiple");
                return tip;
            }
            tip.replace(index, index+12, question.getUserAnswers().get(0));
        }
        return tip;
    }
}
