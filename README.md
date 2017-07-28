# Streaming examples using Play 2.5 and Akka Streams

## You can read the tutorial [here](http://loicdescotte.github.io/posts/play25-akka-streams/)

## Max additions
Added two modules: one for the fake feed (tweeterFeed) and one for the consumer app (consumer).

In order to run from sbt do in one terminal:
```
sbt>
> tweeterFeed/run 9001
```

and in another terminal run the consumer:
```
> consumer/run
```

