package model.reactive.event

import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Concurrent.Channel

object EventProducer {
    def apply[T](playProducer: (Enumerator[T], Channel[T])) = new EventProducer(playProducer)
}

class EventProducer[T](playProducer: (Enumerator[T], Channel[T])) {
    def enumerator: Enumerator[T] = playProducer._1
    def channel: Channel[T] = playProducer._2
}