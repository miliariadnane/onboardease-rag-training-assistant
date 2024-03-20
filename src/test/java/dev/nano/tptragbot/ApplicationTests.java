package dev.nano.tptragbot;

import dev.nano.tptragbot.langchain.service.TPTBotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class ApplicationTests {

	@Autowired
	private TPTBotService tptBotService;

	@Test
	void testQuestions() {
		List<String> questions = List.of(
			//"How can I use Inline Element in XLIFF 1.2?",
			//"Can I use XLIFF 1.2 in my project?",
			"What is the branching strategy (release) in our company?"
		);

		for (String question : questions) {
			String answer = tptBotService.askQuestion(question);
			System.out.println("Question: " + question);
			System.out.println("Answer: " + answer);
			System.out.println("======================================================");
		}
	}

}
