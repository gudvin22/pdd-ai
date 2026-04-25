package com.pdd.parser;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

public class PddParser {

    public static void main(String[] args) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://xn----7sbnackuskv0m.xn--p1ai/?bilet=1")).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Размер страницы: " + response.body().length());

        String html = response.body();
        int startPos = html.indexOf("quest_id[0]=");
        System.out.println("Позиция начала quest_id[0]= : " + startPos);

        //Кусок html начиная с startPos
        int endSnippet = Math.min(startPos + 500, html.length());
        String snippet = html.substring(startPos, endSnippet);
        System.out.println("Отрывок:\n" + snippet);

        // Находим начало и конец блока с объявлениями
        int startBlock = html.indexOf("quest_id[0]=");
        int endBlock = html.indexOf("for (i=0; i < quest_length; i++){", startBlock);
        if (endBlock == -1) {
            // Если не найдено, попробуем другой шаблон
            endBlock = html.indexOf("for(i=0;i<quest_length;i++){", startBlock);
        }
        String scriptBlock = html.substring(startBlock, endBlock);
        System.out.println("Длина блока объявлений: " + scriptBlock.length());
        System.out.println("Первые 300 символов блока:");
        System.out.println(scriptBlock.substring(0, Math.min(300, scriptBlock.length())));

        // Выводим конец блока
        int len = scriptBlock.length();
        int startLast = Math.max(0, len - 500);
        String lastPart = scriptBlock.substring(startLast, len);
        System.out.println("Последние 500 символов блока:\n" + lastPart);

    }
}
