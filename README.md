Travis: ![alt text][build-status]

Setup
=====
* createuser -P devnull
* createdb devnull
* sbt run

Running Tests
=============

Run unit test

````
sbt test
````

Test that is tagged as database test. Require that you have a postgres database

````
sbt db:test
````

Unit test, database test and test that is tagged as slow.

````
sbt all:test
````

Services
========

Add feedback
------------

Endpoint: 
`POST events/<eventId>/sessions/<sessionId>/feedbacks`

Headers:

| Header       | Required | Description                |
|:-------------|:-------- |:-------------------------- |
| Voter-ID     | Yes      | Used to identify the voter |
| User-Agent   | No       | Identify the client        |

Content:

application/vnd.collection+json:

```
{
  "template": {
    "data": [
      {"name": "overall",   "value" : 1},
      {"name": "relevance", "value" : 1},
      {"name": "content",   "value" : 1},
      {"name": "quality",   "value" : 1},
    ]
  }
}
```

application/json:

```
{
  "overall" : 1,
  "relevance" : 1,
  "content" : 1,
  "quality" : 1
}
```

Valid input in the value parameter is an integer in the range 0 to 5 where 5 is the best score.

Examples
========

* Add feedback
````
$ curl 'http://localhost:8082/server/events/1234/sessions/5678/feedbacks' \
-H 'Content-Type: application/vnd.collection+json' \
-H 'Voter-ID: some-voter' \
--data-binary $'{\n  "template": {\n "data": [\n      {"name": "overall", "value" : 1},\n      {"name": "relevance", "value" : 1},\n      {"name": "content", "value" : 1},\n {"name": "quality", "value" : 1},\n      ]\n  }\n}'
````


[build-status]: https://travis-ci.org/javaBin/devnull.svg "Build Status"
