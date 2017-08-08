package scheduler

import org.quartz.{ JobExecutionContext, Job }
import models.daos.ElectionDAOImpl
import models.Election

import java.util.Date

class UpdateTable extends Job {
  val todayDate = new Date()
  val electionDAOImpl = new ElectionDAOImpl()

  def execute(context: JobExecutionContext) {
    val inActiveElections = electionDAOImpl.getInactiveElections()
    for (i <- inActiveElections) {
      if (todayDate.after(i.start) || todayDate.equals(i.start)) {
        electionDAOImpl.updateActiveElection(i.id)
      }
    }

    val activeElections = electionDAOImpl.getActiveElection()
    for (i <- activeElections) {
      if (todayDate.after(i.end) || todayDate.equals(i.end)) {
        electionDAOImpl.updateCompleteElection(i.id)
      }
    }

    val completedElections = electionDAOImpl.getCompletedElections()
    for (i <- completedElections) {
      //  countVotes
      //update elections
    }

  }

}
