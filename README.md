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

Setup a local database with docker:
```
docker run --name devnull -e POSTGRES_PASSWORD=devnull -e POSTGRES_USER=devnull -p 5432:5432 -d postgres
```

Services
========

Add feedback to a session
-------------------------

**Endpoint:** 
`POST events/<eventId>/sessions/<sessionId>/feedbacks`

**Headers:**

| Header       | Required | Description                |
|:-------------|:-------- |:-------------------------- |
| Voter-ID     | Yes      | Used to identify the voter |
| User-Agent   | No       | Identify the client        |

**Content:**

 application/json:

```
{
  "overall" : 1,
  "relevance" : 1,
  "content" : 1,
  "quality" : 1,
  "comments": "A Comment"
}
```

For the rating the valid input in the value parameter is an integer in the range 0 to 5 where 5 is the best score.
The comment is optional but must be a string. 

Add feedback to an event
------------------------

**Endpoint:** 
`POST events/<eventId>/`

**Headers:**

| Header       | Required | Description                |
|:-------------|:-------- |:-------------------------- |
| Token        | Yes      | Security token             |


**Content:**

application/json:

```
[
  {
    sessionId: "UUID",
    green: 1,
    yellow: 1,
    red: 1,
    participants: 1
  }
]
```

Examples
========

* Add feedback
````
$ curl 'http://localhost:8082/server/events/1234/sessions/5678/feedbacks' \
-H 'Content-Type: application/json' \
-H 'Voter-ID: some-voter' \
--data-binary $'{ "overall" : 1, "relevance" : 1, "content" : 1, "quality" : 1, "comments": "A Comment" }'
````

{ "overall" : 1, "relevance" : 1, "content" : 1, "quality" : 1, "comments": "A Comment" }

[build-status]: https://travis-ci.org/javaBin/devnull.svg "Build Status"
