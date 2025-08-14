package io.jenkins.plugins;

import java.io.IOException;

import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import hudson.model.Action;

@ExportedBean
public class ChatGptBuildAction implements Action {

	private final String result;
	private final String apiKey;
	private final String model;

	public ChatGptBuildAction(String result, String apiKey, String model) {
		this.result = result;
		this.apiKey = apiKey;
		this.model = model;
	}

	@Exported
	public String getIconFileName() {
//		return "/plugin/jenkins-theme/images/24x24/report.png";
		return "https://cdn.hugeicons.com/icons/chat-gpt-bulk-rounded.svg?v=2.0";
	}

	@Exported
	public String getDisplayName() {
		return "ChatGPT 분석 결과";
	}

	@Exported
	public String getUrlName() {
		return "chatgpt-analysis";
	}

	public String getResult() {
		return result;
	}

	public HttpResponse doAskQuestion(@QueryParameter String query) {
		if (query == null || query.trim().isEmpty()) {
			return HttpResponses.text("질문을 입력해주세요.");
		}

		try {
			String answer = PluginUtil.callOpenAI(query, this.apiKey, this.model); // ChatGPT API 호출
			return HttpResponses.text(answer);
		} catch (IOException e) {
			return HttpResponses.text("GPT 호출 중 오류: " + e.getMessage());
		}
	}

	/*
	@RequirePOST
	public HttpResponse doAskQuestion() throws ServletException {
		try {
			JSONObject json = Stapler.getCurrentRequest2().getSubmittedForm(); // JSON body 파싱
			String query = json.getString("query");

			if (query == null || query.trim().isEmpty()) {
				return HttpResponses.text("질문을 입력해주세요.");
			}

			String answer = PluginUtil.callOpenAI(query, this.apiKey, this.model);
			return HttpResponses.text(answer);

		} catch (IOException e) {
			return HttpResponses.text("GPT 호출 중 오류: " + e.getMessage());
		}
	}
	*/
}
