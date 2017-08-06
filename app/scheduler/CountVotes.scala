package scheduler

import org.quartz.{JobExecutionContext, Job}

class CountVotes extends Job {
  def execute(context: JobExecutionContext) {
    // println("Hello")
  }
}
