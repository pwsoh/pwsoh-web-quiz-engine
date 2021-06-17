package engine.quiz;

import engine.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;


@RestController
public class QuizController {

    private final QuizRepository quizRepository;
    private final SubmissionRepository submissionRepository;

    @Autowired
    public QuizController(QuizRepository quizRepository, SubmissionRepository submissionRepository) {
        this.quizRepository = quizRepository;
        this.submissionRepository = submissionRepository;
    }

    @PostMapping(value = "/api/quizzes", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object addQuiz(@RequestBody @Valid @NotNull Quiz quiz, @AuthenticationPrincipal User user) {
        try{
            quiz.setAuthor(user);
            return quizRepository.save(quiz);
        } catch (Exception e) {
            return new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/api/quizzes/{id}")
    public Quiz getQuizByID(@PathVariable int id, HttpServletResponse response) {
        return quizRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));
    }

    Page<Quiz> getAllQuizzes(int pageNo, int pageSize) {
        Pageable paging = PageRequest.of(pageNo,pageSize);
        return quizRepository.findAll(paging);
    }

    @GetMapping("/api/quizzes")
    public @ResponseBody ResponseEntity<Page<Quiz>> getQuizzes (
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ){
        return new ResponseEntity<Page<Quiz>>(
                getAllQuizzes(page, pageSize),
                HttpStatus.OK
        );
    }

    @GetMapping("/api/quizzes/completed")
    public Page<Submission> getCompletedQuizzes (
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @AuthenticationPrincipal User user
    ) {
        String username = user.getEmail();
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("timestamp").descending());
        return submissionRepository.getSubmissions(username, pageable);
    }


    @PostMapping(value="/api/quizzes/{id}/solve")
    public Feedback getAnsByID(@RequestBody Map<String,int[]> data, @PathVariable int id,  @AuthenticationPrincipal User user, HttpServletResponse response) {
        int[] tmp = data.getOrDefault("answer", null);
        Answer answer = new Answer(tmp);
        int[] quizAns;
        try{
            Quiz q = quizRepository.findById(id).orElseThrow(Exception::new);

            if (q.getAnswer() == null) {
                quizAns = new int[0];
            } else {
                quizAns = Arrays.copyOf(q.getAnswer(), q.getAnswer().length);
            }
            int[] arr1 = Arrays.copyOf(answer.getAnswer(), answer.getAnswer().length);
            Arrays.sort(arr1);
            Arrays.sort(quizAns);
            if (Arrays.equals(arr1, quizAns)) {
                Submission submission = new Submission(user.getEmail(), id, System.currentTimeMillis());
                submissionRepository.save(submission);
            }
            return new Feedback(Arrays.equals(arr1, quizAns));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    @DeleteMapping(value = "/api/quizzes/{id}")
    public ResponseEntity<Object> deleteQuiz(@PathVariable int id, @AuthenticationPrincipal User user) {
        Optional<Quiz> quiz = quizRepository.findById(id);
        if (quiz.isPresent()) {
            if (quiz.get().getAuthor().getEmail().equals(user.getEmail())) {
                quizRepository.delete(quiz.get());
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}