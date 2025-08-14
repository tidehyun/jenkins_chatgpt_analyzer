package io.jenkins.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import net.sf.json.JSONObject;

public class PluginUtil {

	public static String callOpenAI(String prompt, String apiKey, String model) throws IOException {
		String endpoint = "https://api.openai.com/v1/chat/completions";
		String requestBody = "{\n" + "  \"model\": \"" + model + "\",\n" + "  \"messages\": [\n"
				+ "    {\"role\": \"system\", \"content\": \"당신은 소프트웨어 빌드 전문가입니다. 핵심만 정리해서 한글로 답변 해주고 메시지를 사용자 친화적으로 표현해줘 이모티콘 활용해서~\"},\n"
				+ "    {\"role\": \"user\", \"content\": \"" + escapeJson(prompt) + "\"}\n" + "  ]\n" + "}";

		URL url = new URL(endpoint);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Authorization", "Bearer " + apiKey);

		// Request body 전송
		try (OutputStream os = conn.getOutputStream()) {
			byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
			os.write(input);
		}

		// 응답 처리
		int status = conn.getResponseCode();
		InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();

		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		StringBuilder response = new StringBuilder();
		String line;
		while ((line = in.readLine()) != null) {
			response.append(line);
		}
		in.close();
		conn.disconnect();
		is.close();

		JSONObject json = JSONObject.fromObject(response.toString());
		String result = json.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");

		return result;
	}

	private static String escapeJson(String text) {
		return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t",
				"\\t");
	}

}
