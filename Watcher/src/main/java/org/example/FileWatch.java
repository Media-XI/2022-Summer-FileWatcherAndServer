package org.example;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Scanner;

public class FileWatch {
    private WatchService watchService;
    private Path path;

    private static Integer createdFileCount = 0;

    private String serverUrl = "http://localhost:8080/upload";

    public FileWatch() {
    }

    public void create() throws IOException {
        Scanner sc = new Scanner(System.in);
        watchService = FileSystems.getDefault().newWatchService();
        System.out.println("서버 경로를 입력해주세요(입력안할 시 기본 URL) :");
        String tempUrl = sc.nextLine();
        if (!tempUrl.equals("")) {
            serverUrl = tempUrl;
        }
        System.out.println(serverUrl + "로 연결합니다");

        System.out.print("경로를 입력해주세요 : ");
        String filePath = sc.nextLine();

        path = Paths.get(filePath);

        path.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.OVERFLOW
        );
    }

    public void run() throws Exception {
        String watchPath = path.getParent() + "/" + path.getFileName() + "/";
        System.out.println("감시하는 경로 : " + watchPath + "\n");

        while (true) {
            WatchKey key = watchService.take();
            List<WatchEvent<?>> watchEvents = key.pollEvents();

            for (WatchEvent<?> event : watchEvents) {
                WatchEvent.Kind<?> kind = event.kind();
                Path newFIle = (Path) event.context();
                Path absolutePath = newFIle.toAbsolutePath();

                if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                    String filepaths = watchPath + newFIle.getFileName().toString();
                    System.out.println(String.format("[%d번] 파일 생성됨 : %s", ++createdFileCount, newFIle.getFileName()));
                    fileSendToServer(newFIle.getFileName().toString(),filepaths);

                } else if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                    System.out.println("파일 수정됨 : " + newFIle.getFileName() + "\n");
                } else if (kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
                    System.out.println("파일 삭제됨 : " + newFIle.getFileName() + "\n");
                } else if (kind.equals(StandardWatchEventKinds.OVERFLOW)) {
                    System.out.println("Overflow \n");
                }
            }
            if (!key.reset()) {
                try {
                    watchService.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void fileSendToServer(String fileName, String currentFilePath){
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart(
                        "file", // 파라미터
                        fileName,
                        RequestBody.create(MediaType.parse("text/csv"), new File(currentFilePath))
                ).build();
        Request request = new Request.Builder()
                .url(serverUrl)
                .post(requestBody)
                .build();
        System.out.println(String.format("파일 경로 : %s", currentFilePath));

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("전송 실패 : " + call.request().toString());
                System.out.println("에러 내용 : " + e.getMessage());
                System.out.println();

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                System.out.println("전송 응답 : " + response.body().string());
                System.out.println();
            }
        });
    }

}
