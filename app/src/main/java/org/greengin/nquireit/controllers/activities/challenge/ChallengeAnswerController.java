package org.greengin.nquireit.controllers.activities.challenge;

import  java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.mangofactory.jsonview.ResponseView;
import org.greengin.nquireit.entities.activities.challenge.ChallengeAnswer;
import org.greengin.nquireit.entities.projects.Project;
import org.greengin.nquireit.json.Views;
import org.greengin.nquireit.logic.mail.Mailer;
import org.greengin.nquireit.logic.project.challenge.ChallengeActivityActions;
import org.greengin.nquireit.logic.project.challenge.ChallengeAnswerRequest;
import org.greengin.nquireit.logic.project.challenge.NewChallengeAnswerResponse;
import org.greengin.nquireit.logic.rating.VoteCount;
import org.greengin.nquireit.logic.rating.VoteRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(value = "/api/project/{projectId}/challenge/answers")
public class ChallengeAnswerController extends AbstractChallengeController{


    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    @ResponseView(value = Views.VotableCount.class)
	public Collection<ChallengeAnswer> get(@PathVariable("projectId") Long projectId, HttpServletRequest request) {
        return createManager(projectId, request).getAnswersForParticipant();
	}

    @RequestMapping(value = "/{answerId}/vote", method = RequestMethod.POST)
    @ResponseBody
    @ResponseView(value = Views.VotableCount.class)
    public VoteCount vote(@PathVariable("projectId") Long projectId, @PathVariable("answerId") Long answerId, @RequestBody VoteRequest voteData, HttpServletRequest request) {
        ChallengeActivityActions voter = createManager(projectId, request);
        return voter.vote(answerId, voteData);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
	public NewChallengeAnswerResponse create(@PathVariable("projectId") Long projectId, @RequestBody ChallengeAnswerRequest answerData, HttpServletRequest request) {
        return createManager(projectId, request).createAnswer(answerData);
	}

	@RequestMapping(value = "/{answerId}", method = RequestMethod.PUT)
    @ResponseBody
	public Collection<ChallengeAnswer> update(@PathVariable("projectId") Long projectId, @PathVariable("answerId") Long answerId, @RequestBody ChallengeAnswerRequest answerData, HttpServletRequest request) {
        return createManager(projectId, request).updateAnswer(answerId, answerData);
	}

	@RequestMapping(value = "/{answerId}", method = RequestMethod.DELETE)
    @ResponseBody
	public Collection<ChallengeAnswer> delete(@PathVariable("projectId") Long projectId, @PathVariable("answerId") Long answerId, HttpServletRequest request) {
        return createManager(projectId, request).deleteAnswer(answerId);
	}

    @RequestMapping(value = "/{answerId}/submit", method = RequestMethod.POST)
    @ResponseBody
	public Collection<ChallengeAnswer> submit(@PathVariable("projectId") Long projectId, @PathVariable("answerId") Long answerId, HttpServletRequest request) {

        Project project = context.getProjectDao().project(projectId);

        Mailer mailer = new Mailer();
        mailer.sendMail(
            "New mission idea - " + project.getTitle(),
            "Hello nQuire-it user,\n\n" +
            "A new idea has been added to the nQuire-it mission '" + project.getTitle() + "':\n" +
            "http://www.nquire-it.org/#/project/" + projectId + "/challenge\n\n" +
            "To stop receiving these messages, update your notification preferences at:\n" +
            "http://www.nquire-it.org/#/profile\n\n" +
            "Warm regards,\nnQuire-it team",
            context.getUserProfileDao().projectNotifications(projectId),
            false
        );

        return createManager(projectId, request).submitAnswer(answerId, true);
	}

    @RequestMapping(value = "/{answerId}/withdraw", method = RequestMethod.POST)
    @ResponseBody
	public Collection<ChallengeAnswer> withdraw(@PathVariable("projectId") Long projectId, @PathVariable("answerId") Long answerId, HttpServletRequest request) {
        return createManager(projectId, request).submitAnswer(answerId, false);
	}

}
