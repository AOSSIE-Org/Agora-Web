package scheduler

import org.quartz.CronScheduleBuilder._
import org.quartz.JobBuilder.newJob
import org.quartz.TriggerBuilder._
import org.quartz.impl.StdSchedulerFactory

object Scheduler {
  val scheduler = StdSchedulerFactory.getDefaultScheduler()

  // def CountVotesDaily() {
  //   val job = newJob(classOf[CountVotes]).build()
  //   val trigger = newTrigger().withSchedule(cronSchedule("0 0 12 * * ?")).build()
  //   scheduler.scheduleJob(job, trigger)
  // }

  def UpdateTableDaily() {
    val job = newJob(classOf[UpdateTable]).build()
    val trigger = newTrigger().withSchedule(cronSchedule("0 0 12 * * ?")).build()
    scheduler.scheduleJob(job, trigger)
  }

}
