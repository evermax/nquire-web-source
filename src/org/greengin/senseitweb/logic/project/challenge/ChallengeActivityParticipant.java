package org.greengin.senseitweb.logic.project.challenge;

import java.util.Collection;

import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;

import org.greengin.senseitweb.entities.activities.challenge.ChallengeActivity;
import org.greengin.senseitweb.entities.activities.challenge.ChallengeAnswer;
import org.greengin.senseitweb.logic.project.AbstractActivityParticipant;

public class ChallengeActivityParticipant extends AbstractActivityParticipant<ChallengeActivity> {
	private static final String MY_ANSWERS_QUERY = String.format(
			"SELECT a FROM %s a WHERE a.project = :project AND a.author = :author", ChallengeAnswer.class.getName());

	private static final String ALL_ANSWERS_QUERY = String.format("SELECT a FROM %s a WHERE a.project = :project",
			ChallengeAnswer.class.getName());

	private static final String ANSWER_COUNT_QUERY = String.format(
			"SELECT COUNT(a) AS N FROM %s a WHERE a.project = :project AND a.author = :author",
			ChallengeAnswer.class.getName());

	public ChallengeActivityParticipant(Long projectId, HttpServletRequest request) {
		super(projectId, request, ChallengeActivity.class);
	}

	private Collection<ChallengeAnswer> getAnswers(boolean onlyMine) {
		if (hasAccess) {
			TypedQuery<ChallengeAnswer> query = em.createQuery(onlyMine ? MY_ANSWERS_QUERY : ALL_ANSWERS_QUERY,
					ChallengeAnswer.class);
			query.setParameter("project", project);
			if (onlyMine) {
				query.setParameter("author", user);
			}
			return query.getResultList();
		} else {
			return null;
		}

	}

	public Collection<ChallengeAnswer> getMyAnswers() {
		return getAnswers(true);
	}

	public Collection<ChallengeAnswer> getAllAnswers() {
		return getAnswers(false);
	}

	public NewChallengeAnswerResponse createAnswer(ChallengeAnswerRequest answerData) {
		if (hasAccess) {
			TypedQuery<Long> query = em.createQuery(ANSWER_COUNT_QUERY, Long.class);
			query.setParameter("project", project);
			query.setParameter("author", user);
			Long n = query.getSingleResult();

			if (n < activity.getMaxAnswers()) {
				em.getTransaction().begin();
				ChallengeAnswer answer = new ChallengeAnswer();
				answer.setAuthor(user);
				answer.setProject(project);
				answerData.update(answer);
				em.persist(answer);
				em.getTransaction().commit();

				NewChallengeAnswerResponse response = new NewChallengeAnswerResponse();
				response.setAnswers(getMyAnswers());
				response.setNewAnswer(answer.getId());
				return response;
			}

		}

		return null;
	}

	public Collection<ChallengeAnswer> updateAnswer(Long answerId, ChallengeAnswerRequest answerData) {
		if (hasAccess) {
			em.getTransaction().begin();
			ChallengeAnswer answer = em.find(ChallengeAnswer.class, answerId);
			answerData.update(answer);
			em.getTransaction().commit();

			return getMyAnswers();
		} else {
			return null;
		}
	}

	public Collection<ChallengeAnswer> deleteAnswer(Long answerId) {
		if (hasAccess) {
			em.getTransaction().begin();
			ChallengeAnswer answer = em.find(ChallengeAnswer.class, answerId);
			em.remove(answer);
			em.getTransaction().commit();

			return getMyAnswers();
		} else {
			return null;
		}
	}
}
