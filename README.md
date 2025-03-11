# hello-rabbitmq

## RabbitMQ
RabbitMQs web-console is reachable at <http://localhost:8080>.

## Receiver
The simple will wait for messages, and upon receiving, log it to the console. To watch the console log do `make logs`.

## Sender1
You can make Sender1 publish a message to the channel by POSTing a request with a body to it's API:
```
curl -H "Content-Type: application/json" -d '{"question":"wtf?"}' -X POST http://localhost:8081
```
## Sender 2
Sender2 will (re)send publish a message to the queue "hello" every second.


## Check health
```
docker inspect --format "{{json .State.Health }}" <container name> | jq
```
