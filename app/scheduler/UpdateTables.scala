package scheduler

import org.quartz.{ JobExecutionContext, Job }
import models.daos.ElectionDAOImpl
import models.daos.ResultFileDAOImpl
import models.Election

import java.util.Date

class UpdateTable extends Job {
  val todayDate = new Date()
  val electionDAOImpl = new ElectionDAOImpl()
  val resultFileDAOImpl = new ResultFileDAOImpl()

  def execute(context: JobExecutionContext) {
    val inActiveElections = electionDAOImpl.getInactiveElections()
    for (election <- inActiveElections) {
      if (todayDate.after(election.start) || todayDate.equals(election.start)) {
        electionDAOImpl.updateActiveElection(election.id)
      }
    }

    val activeElections = electionDAOImpl.getActiveElection()
    for (election <- activeElections) {
      if (todayDate.after(election.end) || todayDate.equals(election.end)) {
        electionDAOImpl.updateCompleteElection(election.id)
      }
    }

    val completedElections = electionDAOImpl.getCompletedElections()
    for (election <- completedElections) {
      resultFileDAOImpl.saveResult(election.ballot,"EVACS",election.candidates,election.id)
    }

  }

}
