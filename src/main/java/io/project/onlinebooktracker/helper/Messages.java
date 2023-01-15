package io.project.onlinebooktracker.helper;

public class Messages {
    
    private String content;
	private String type;
	
	// parameterized constructor
	public Messages(String content, String type) {
		super();
		this.content = content;
		this.type = type;
	}
	
	// getters and setters
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
