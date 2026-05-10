package com.pdd.parser;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import com.pdd.pddai.dto.QuestionDto;
import tools.jackson.databind.ObjectMapper;

public class PddParser {

    public static void main(String[] args) throws IOException, InterruptedException {

        List<QuestionDto> questions = new ArrayList<>();

        HttpClient client = HttpClient.newHttpClient();
        String address = "https://xn----7sbnackuskv0m.xn--p1ai/?bilet=";

        for (int ticket = 111; ticket <= 40; ticket++) {            // чтоб не запустилось

            String addressTicket = address + ticket;
            HttpRequest request = HttpRequest.newBuilder(URI.create(addressTicket)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Размер страницы: " + response.body().length());

            String html = response.body();
            int startPos = html.indexOf("quest_id[0]=");
            System.out.println("Позиция начала quest_id[0]= : " + startPos);


            // Находим начало и конец блока с объявлениями
            int startBlock = html.indexOf("quest_id[0]=");
            int endBlock = html.indexOf("for (i=0; i < quest_length; i++){", startBlock);
            if (endBlock == -1) {
                // Если не найдено, попробуем другой шаблон
                endBlock = html.indexOf("for(i=0;i<quest_length;i++){", startBlock);
            }
            String scriptBlock = html.substring(startBlock, endBlock);
            System.out.println("Длина блока объявлений: " + scriptBlock.length());

            // Разбиваем блок на фрагменты по "quest_id[цифра]="
            String[] rawParts = scriptBlock.split("quest_id\\[\\d+\\]=");


            System.out.println("=== Извлечение номера билета ===");
            for (int i = 1; i < rawParts.length; i++) {
                String fragment = rawParts[i];
                int questIdx = i - 1;
                int ticketNumber = extractIntValue(fragment, "quest_idb", questIdx);    //извлекаем номер билета
                int questionNumber = extractIntValue(fragment, "quest_numberq", questIdx);  //извлекаем номер вопроса
                String questionText = extractValue(fragment, "quest_quest", questIdx);

                String correctAnswerStr = extractValue(fragment, "a_t", questIdx);
                int correctAnswerIndex = correctAnswerStr != null ? Integer.parseInt(correctAnswerStr) : -1;
                String imageUrlSmall = extractValue(fragment, "quest_image_name", questIdx);
                String imageUrlBig = extractValue(fragment, "quest_image_nameb", questIdx);
                String answersCountStr = extractValue(fragment, "a_length", questIdx);  // количество ответов в вопросе
                int answersCount = answersCountStr != null ? Integer.parseInt(answersCountStr) : -1;

                List<String> answers = new ArrayList<>();
                for (int j = 0; j < answersCount; j++) {

                    String answer = extractValueWithTwoIndices(fragment, "a_answers", questIdx, j);
                    answers.add(answer);

                }

                System.out.println(
                        "Вопрос " + questIdx +
                                " -> билет: " + ticketNumber +
                                ", номер вопроса: " + questionNumber +
                                ", вопрос: " + questionText +
                                ", Индекс правильного ответа: " + correctAnswerIndex +
                                ", Ссылка маленькая картинка: " + imageUrlSmall +
                                ", Ссылка большая картинка: \"" + imageUrlBig +
                                ", Ответов : " + answersCountStr +
                                ", Ответы: " + answers
                );


                // кладем эти данные в QuestionDto
                QuestionDto questionDto = new QuestionDto();
                questionDto.setTicketNumber(ticketNumber);
                questionDto.setQuestionNumber(questionNumber);
                questionDto.setQuestionText(questionText);
                questionDto.setRightAnswerNumber(correctAnswerIndex);
                questionDto.setAnswersText(answers);
                questionDto.setImageUrlSmall(imageUrlSmall);
                questionDto.setImageUrlBig(imageUrlBig);
                questionDto.setCategory("ABM");

                questions.add(questionDto);         // положили QuestionDto в массив

            }

        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new java.io.File("questions.json"), questions);
        Thread.sleep(2000); // пауза 2 секундs между запросами
    }


    private static String extractValue(String fragment, String key, int index) {
        String search = key + "[" + index + "]=\"";
        int start = fragment.indexOf(search);
        if (start == -1) return null;
        start += search.length();
        int end = fragment.indexOf("\"", start);
        if (end == -1) return null;
        return fragment.substring(start, end);
    }

    private static int extractIntValue(String fragment, String key, int index) {
        String search = key + "[" + index + "]=";
        int start = fragment.indexOf(search);
        if (start == -1) return -1;
        start += search.length();
        // Ищем конец числа – точку с запятой или пробел
        int end = start;
        while (end < fragment.length() && Character.isDigit(fragment.charAt(end))) {
            end++;
        }
        if (end == start) return -1;
        String numStr = fragment.substring(start, end);
        try {
            return Integer.parseInt(numStr);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static String extractValueWithTwoIndices(String fragment, String key, int i, int j) {
        String search = key + "[" + i + "][" + j + "]=\"";
        int start = fragment.indexOf(search);
        if (start == -1) return null;
        start += search.length();
        int end = fragment.indexOf("\"", start);
        if (end == -1) return null;
        return fragment.substring(start, end);
    }

}
