package com.pdd.parser;

import com.pdd.pddai.dto.QuestionDto;
import tools.jackson.databind.ObjectMapper;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ImageDownloader {

    public static void main(String[] args) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        Path jsonFile = Paths.get("src\\main\\resources\\questions.json");

        if ((!Files.exists(jsonFile))) {
            System.err.println("Файл questions.json не найден в текущей директории: " +
                    jsonFile.toAbsolutePath());
            return;
        }

        //Читаем JSON из файла и превращаем его в список объектов QuestionDto.
        List<QuestionDto> dtos = mapper.readValue(jsonFile.toFile(),
                mapper.getTypeFactory().constructCollectionType(List.class, QuestionDto.class));

        System.out.println("Загружено записей: " + dtos.size());

        Path baseDir = Paths.get("src/main/resources/static/images-bilety");

        //если папки нет _ создать ее

        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);
            System.out.println("Создана папка: " + baseDir.toAbsolutePath());
        }

        HttpClient client = HttpClient.newHttpClient();
        String host = "https://xn----7sbnackuskv0m.xn--p1ai";

        int downloaded = 0;
        int skipped = 0;

        for (QuestionDto dto : dtos) {
           // String urlPathBig = dto.getImageUrlBig();
            String urlPathSmall = dto.getImageUrlSmall();

            if(urlPathSmall == null || urlPathSmall.trim().isEmpty() || "0".equals(urlPathSmall)) {
                continue;
            }

            // имя файла
            String fileName = Paths.get(urlPathSmall).getFileName().toString();

            // Формируем путь к локальному файлу (базовая папка + имя файла)

            Path localFile = baseDir.resolve(fileName);

            // если такой файл там есть, то скипаем его
            if(Files.exists(localFile)) {
                System.out.println("Уже есть: " + fileName);
                skipped++;
                continue;
            }

            String fullUrl = host + urlPathSmall;

            // Создаём HTTP-запрос типа GET по указанному URL.
            HttpRequest request = HttpRequest.newBuilder(URI.create(fullUrl)).GET().build();

            // HttpResponse.BodyHandlers.ofByteArray() –
            // читаем тело ответа как массив байтов (картинка).
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 200) {
                Files.write(localFile, response.body());
                System.out.println("Скачано: " + fileName);
                downloaded++;
            } else {
                System.err.println("Ошибка скачивания " + fullUrl + " : " + response.statusCode());
            }
            Thread.sleep(1000);
        }

        System.out.println("Готово. Скачано новых: " + downloaded + ", пропущено (уже есть): " + skipped);



    }
}
