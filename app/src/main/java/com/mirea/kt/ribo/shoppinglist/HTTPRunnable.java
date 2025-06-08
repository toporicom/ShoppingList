package com.mirea.kt.ribo.shoppinglist;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
//HTTPRunnable — это пользовательский класс, реализующий интерфейс Runnable (предназначен для выполнения кода в отдельном потоке)
public class HTTPRunnable implements Runnable {

    private String address; // URL адрес для запроса
    private HashMap<String, String> requestBody; // Тело запроса в виде пар ключ-значение
    private String responseBody; // Ответ от сервера
    private String generateStringBody(){ //Этот метод преобразует HashMap с параметрами запроса в строку формата "x-www-form-urlencoded",
        // которая используется в HTTP-запросах.
        StringBuilder sbParams = new StringBuilder();

        if (this.requestBody != null && !requestBody.isEmpty()){
            int i = 0;
            for (String key : this.requestBody.keySet()){ //перебирает все ключи в HashMap
                try {
                    if (i != 0){
                        sbParams.append("&"); // добавляет "&" между параметрами, но не перед первым
                    }
                    sbParams.append(key).append("=").append(URLEncoder.encode(this.requestBody.get(key), "UTF-8")); //добавляет
                    //имя параметра и знак равенства; кодирует значение параметра:
                    //Заменяет пробелы на "+"
                    //Кодирует специальные символы в %-последовательности (Знак равно = → %3D; Пробел → %20 или +)
                    //Использует кодировку UTF-8
                } catch (UnsupportedEncodingException e) { //возникает при проблемах с кодировкой UTF-8
                    throw new RuntimeException(e);
                }
                i++;
            }
        }
        return sbParams.toString(); //возвращает строку вида "param1=value1&param2=value2"
    }

    public HTTPRunnable(String address, HashMap<String, String> requestBody) {
        this.address = address;
        this.requestBody = requestBody;
    }

    public String getResponseBody() {
        return responseBody;
    }

    @Override
    public void run() {
        if(this.address != null && !this.address.isEmpty()){
            try {
                URL url = new URL(this.address);
                URLConnection connection = url.openConnection(); //Открывает соединение
                HttpURLConnection httpURLConnection = (HttpURLConnection) connection; //приводит его к HttpURLConnection
                httpURLConnection.setRequestMethod("POST"); //Устанавливает метод запроса POST
                httpURLConnection.setDoOutput(true); //Разрешает отправку данных в теле запроса
                OutputStreamWriter osw = new OutputStreamWriter(httpURLConnection.getOutputStream());//Создает поток для записи данных
                osw.write(generateStringBody()); //Записывает сформированное тело запроса (из метода generateStringBody())
                osw.flush(); //гарантирует немедленную отправку данных
                int responseCode = httpURLConnection.getResponseCode(); // Получает код ответа HTTP (200, 404 и т.д.)
                Log.i("my_tag", "Response code: " + responseCode);
                if (responseCode == 200){ // Если 200 то всё хорошо
                    InputStreamReader isr = new InputStreamReader(httpURLConnection.getInputStream()); // Открывает поток чтения получаемых данных
                    BufferedReader br = new BufferedReader(isr); // Класс для возможности чтения данных построчно
                    String currentLine; // Просто переменная, которая понадобится позже
                    StringBuilder sbResponse = new StringBuilder(); // Билдер для создания вывода в виде удобной строчки
                    while ((currentLine = br.readLine()) != null){
                        sbResponse.append(currentLine);
                    }
                    responseBody = sbResponse.toString(); //Сохраняет результат в поле responseBody
                    isr.close();
                }else { // Если код не 200
                    Log.i("my_tag", "Error!");
                }
                osw.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
