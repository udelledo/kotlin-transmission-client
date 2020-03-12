# Test Driven Development and Behavior Driven Development

**Disclaimer:** _this are personal opinions based on my personal experience._

There are many sites that describe how TDD and BDD are supposed to work:

* [Wikipedia](https://en.wikipedia.org/wiki/Test-driven_development)
* [SquashBlog](https://www.squash.io/what-is-test-driven-development-and-how-to-get-it-right/)

Often TDD is associated with Unit tests, but is this the only way?

With this project I must admit I had difficulties to start writing the unit tests so I tried to extract some principles from the sites above and ended up following these principles:

1. [**Lean on compiler**](#lean-on-compiler)
1. [**Integration tests before unit tests**](#integration-tests-before-unit-tests)
1. [**Mocks for mutations**](#mocks-for-mutations)  

## Lean on compiler 
The place to start is `src/test/kotlin`. In hear after creating the first `@Test` function and initializing the `TransmissionClient` my IDE was resulting in errors.
 Fixing the errors with the appropriate objects and methods in `src/main/kotlin` I was _leaning on the compiler_.

I put my `assert(false)` and after reading through the [specifications](https://github.com/transmission/transmission/blob/master/extras/rpc-spec.txt) I ended up with a simple test that was constructing the target URL.


```kotlin
    @Test
    fun `Test transmission client has configurable host`() {
        assert(TransmissionClient("http://localhost").targetUrl == "http://localhost/transmission/rpc")
        assert(TransmissionClient("http://localhost:9091").targetUrl == "http://localhost:9091/transmission/rpc")
        assert(TransmissionClient("http://localhost:9091/customContext").targetUrl == "http://localhost:9091/customContext/rpc")
        assert(TransmissionClient("http://localhost/customContext").targetUrl == "http://localhost/customContext/rpc")
        assert(TransmissionClient("http://localhost/rpc").targetUrl == "http://localhost/rpc")
    }
```

With this code I felt I was doing TDD and I was ready for the next step. This client that can now be initialized has to do something.

Mocking is a very common technique to use in testing, it allows to _mock_ the behavior of complex objects so that when invoking a method on a subject of your tests you can obtain a result without needing the full infrastructure.

It's time to head again to the specs to verify how the interaction with Transmission should be prepare. In there I found all the information that I would need to prepare my **Model classes**, but not many examples on how to mock the actual data that Transmission will send back.

I could have spend time developing the tests based on the specifications and mocking the network interaction with fictional data, but it seemed risky to try and guess how the data would look like, so I opted for running the tests with a real Transmission instance and implementing first some _integration tests_. 

## Integration tests before unit tests
Still following the principle of not developing new features without a test on it and considering the Transmission instance I had available was under basic authentication, I went ahead and wrote my first `@Test` 
 ```kotlin     
@Test
fun `Test transmission client is initialized`() {
    val testSubject = TransmissionClient(transmissionHost, transmissionUser, transmissionPassword)
    val beforeConnecting = testSubject.isInit()
    testSubject.connect()
    assert(beforeConnecting != testSubject.isInit()) { "Client is initialized" }
}
```

The acceptance criteria to pass the test is to verify that the client was initialized with the CSRF token.

The test kept failing until I managed to complete the implementation of the function. WOW this TDD seems nice, if I now try to run the check of how much of my code is covered by test I get a 100% of coverage.

Time for the next step. The first thing I'm interested in is reading the list of torrents, but considering it's a live instance I don't have many guarantees on which values I can assert, so let's start with a simple assertion: **the torrent list should not be empty**.

```kotlin
@Test
fun `Test client can get torrent list`() {
    val testSubject = TransmissionClient(transmissionHost, transmissionUser, transmissionPassword)
    assert(testSubject.getTorrents().isNotEmpty())
}
```

Once again there is a test that is failing until the production code is developed to allow the test to pass.

Now I could advance with the definition of the **model classes** defining  the properties that I could expect according to the specification, however I still wasn't completely sure about the format of values returned by Transmission so I kept having generic assertion to verify that all the methods I needed to interact with Transmission in another side project I'm working on.

* Start torrent
* Stop torrent
* Add torrent
* Remove torrent
* Get session statistics
* Get session parameter
* Set session parameter

Since I was working with a real instance I couldn't rely on assertion on values like ids, status and dates, so I kept the test at minimum trying to keep the tests concise. To make it short I ended up with the following tests.

* Test client can add and delete torrents
* Test client can get session information
* Test client can get torrent list
* Test client can read and change session values
* Test client can start and stop torrents
* Test client can startNow
* Test client parse session

Some tests are sharing method calls in order to prepare the instance so that assertions can be performed, however since it's a live system the tests can fail sometimes due to unexpected and unrelated problems (i.e. request timeout).

To sum up, I have at least one test for each functionality implemented and enough functions, but the tests are not 100% reliable since they sometimes fails without change to the code (i.e. network timeouts) and more importantly a coverage analysis will say that my tests are not covering all the possible combinations that I coded. 

**Maybe now it's time to start thinking about mocks**. With the available tests and the help of a simple function I can now prepare a set of responses for different type of requests and use them in tests that are mocking the network interactions.

In addition to making the tests more reliable being able to assert on fixed values and without a real system will make easier to introduce [mutation testing](https://en.wikipedia.org/wiki/Mutation_testing) in the project.
 
## Mocks for mutations


