package io.jenkins.plugins;

import java.io.IOException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.ListBoxModel;

public class ChatGptAnalyzerBuilder extends Recorder {

	private final String apiKey;
	private final String model;

	@DataBoundConstructor
	public ChatGptAnalyzerBuilder(String apiKey, String model) {
		this.apiKey = apiKey;
		this.model = model;
	}

	public String getApiKey() {
		return apiKey;
	}

	public String getModel() {
		return model;
	}

	@Override
	public boolean perform(@SuppressWarnings("rawtypes") AbstractBuild build, Launcher launcher,
			BuildListener listener) {
		if (build.getResult().equals(Result.SUCCESS)) {
			listener.getLogger().println("[ChatGPT Analyzer] 빌드가 실패하지 않아 분석을 생략합니다.");
			return true;
		} else {
			listener.getLogger().println("[ChatGPT Analyzer] 빌드 로그 분석을 시작합니다...");
		}

		// 빌드 로그 수집
		String buildLog;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			build.getLogText().writeLogTo(0, baos);
			buildLog = baos.toString("UTF-8");
		} catch (IOException e) {
			listener.error("빌드 로그 수집 실패: " + e.getMessage());
			return true;
		}
		String analysis = null;
		try {
			analysis = PluginUtil.callOpenAI(buildLog, this.apiKey, this.model);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (analysis != null) {
			listener.getLogger().println("=== ChatGPT 분석 결과 ===");
			listener.getLogger().println(analysis);
			listener.getLogger().println("=======================");
		} else {
			listener.error("ChatGPT API 호출 실패");
		}

		build.addAction(new ChatGptBuildAction(analysis, this.getApiKey(), this.getModel()));
		return true; // build 자체는 실패시키지 않음
	}

	@Extension
	public static final class ChatGptAnaylzerDescriptor extends BuildStepDescriptor<Publisher> {

		public ChatGptAnaylzerDescriptor() {
			load();
		}

		@Override
		public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "ChatGPT 빌드 로그 분석";
		}

		public ListBoxModel doFillModelItems() {
			ListBoxModel items = new ListBoxModel();
			items.add("gpt-4o", "gpt-4o");
			items.add("gpt-4o-mini", "gpt-4o-mini");
			items.add("gpt-4", "gpt-4");
			items.add("gpt-3.5-turbo", "gpt-3.5-turbo");
			return items;
		}
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

}
