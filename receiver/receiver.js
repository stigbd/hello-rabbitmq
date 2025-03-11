
var rabbit = require('rabbot');


var config = {
  connection: {
    protocol: 'amqp://',
    name: 'default',
    user: process.env.RABBITMQ_DEFAULT_USER,
    pass: process.env.RABBITMQ_DEFAULT_PASS,
    host: 'rabbitmq',
    port: 5672
  },
  exchanges: [
    {
      name:           "worker.exchange",
      type:           "direct",
      autoDelete:     false,
      durable:        true,
      persistent:     true
    },
    {
      name:           "deadLetter.exchange",
      type:           "fanout"
    }
  ],
  queues: [
    {
      name:           "worker.queue",
      autoDelete:     false,
      durable:        true,
      noBatch:        true,
      limit:          1,
      subscribe:      true,
      deadLetter:     'deadLetter.exchange'
    },

    {
      name:           'deadLetter.queue'
    }
  ],
  bindings: [
    {
      exchange:   "worker.exchange",
      target:         "worker.queue",
      keys:           ["email"]
    },

    {
      exchange:      "deadLetter.exchange",
      target:         "deadLetter.queue",
      keys:           ["email"]
    }
  ]
};

var handleMessage = function(payload) {

  if (payload.body.dataOK) {
    // if the data in the payload is good, lets delete the message from the queue
    console.log('payload received: ' + payload.body.msg);
    payload.ack();
  } else {
    // the data is not good, lets move the message to the dead-letter queue
    console.log('rejecting message: ' + payload.body.msg);
    payload.reject();
  }

};

var startListening = function() {

  rabbit.handle({}, handleMessage);

  // must define handler before starting the subscription, otherwise messages will be lost
  rabbit.startSubscription(config.queues[0].name, config.connection.name);
};

// Open a connection and a channel, declare the queue
rabbit
.configure(config)
.then( function() {
  console.log('rabbit is hopping');
  // ready to start rabbit receivers and publishers
  startListening();
})
.then(null, function(err) {
  console.log('Error on configuring rabbit: ', err.stack);
  process.exit(1);
})
.catch( function(err) {
  console.log(err.stack);
  process.exit(1);
});
