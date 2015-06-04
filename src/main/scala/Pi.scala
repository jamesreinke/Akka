import akka.actor._
import akka.routing.RoundRobinRouter


sealed trait PiMessage
case object Calculate extends PiMessage
case class Work(start: Int, numElements: Int) extends PiMessage
case class Result(value: Double) extends PiMessage
case class PiApproximation(pi: Double)



class Worker extends Actor {

	def calculatePiFor(start: Int, numElements: Int): Double = {
		var acc = 0.0
		for(i <- start until (start + numElements))
			acc += 4.0 * (1 - (i % 2) * 2) / (2 * i + 1)
		acc
	}

	def receive = {
		case Work(start, numElements) => 
			sender ! Result(calculatePiFor(start, numElements))
	}
}


class Master(numWorkers: Int, numMessages: Int, numElements: Int, listener: ActorRef) extends Actor {

	var pi: Double = _
	var numResults: Int = _
	val start: Long = System.currentTimeMillis

	val workerRouter = context.actorOf(
		Props[Worker].withRouter(RoundRobinRouter(numWorkers)),
		name = "workerRouter")

	def receive = {
		case Calculate =>
			for (i <- 0 until numMessages) workerRouter ! Work(i * numElements, numElements)
		case Result(value) =>
			pi += value
			numResults += 1
			if(numResults == numMessages) {
				// Send result to the listener
				listener ! PiApproximation(pi)

				//stops this actor and all its supervised children
				context.stop(self)
			}
	}
}

class Listener extends Actor {
	def receive = {
		case PiApproximation(pi) =>
			println("\n\tPi approximiation: \t\t%s"
				.format(pi))
			context.system.shutdown()
	}
}



object Pi extends App {

	calculate(numWorkers = 8, numElements = 10000, numMessages = 10000)

	def calculate(numWorkers: Int, numElements: Int, numMessages: Int) {

		//create an Akka system
		val system = ActorSystem("PiSystem")

		/* 
		create the result listener, which will print the result and shutdown the system 
		we use system.actorOf when we are not in the context of another actor, otherwise we use context.actorOf
		*/
		val listener = system.actorOf(Props[Listener], name = "listener")

		//create the master
		val master = system.actorOf(
		Props(
			new Master(
				numWorkers, 
				numMessages, 
				numElements, 
				listener)),
		name = "master")

		// start the calculation
		master ! Calculate
	}
}