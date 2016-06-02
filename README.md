# ReactiveCaffeineTesting
[![Build Status](https://travis-ci.org/EanLombardo/ReactiveCaffeineTesting.svg?branch=master)](https://travis-ci.org/EanLombardo/ReactiveCaffeineTesting)[ ![Download](https://api.bintray.com/packages/eanlombardo/maven/ReactiveCaffeineTesting/images/download.svg) ](https://bintray.com/eanlombardo/maven/ReactiveCaffeineTesting/_latestVersion)[![License](http://img.shields.io/:license-apache-blue.svg?style=flat-square)](http://www.apache.org/licenses/LICENSE-2.0.html)


ReactiveCaffeineTesting that aims to make complex Observables easy to test while and provide detailed failure reports when tests fail.

ReactiveCaffeineTesting is code meant for testing purposes, for production code you should take a look at [ReactiveCaffeine](https://github.com/EanLombardo/ReactiveCaffeine)

For more information on using ReactiveCaffeineTesting see the [Javadoc](http://eanlombardo.github.io/ReactiveCaffeineTesting/) or the [Wiki](https://github.com/EanLombardo/ReactiveCaffeineTesting/wiki)

## Example
Tests look like this
```Java    
    @Test
    public void someTest() throws Exception{
        final TestSubscriber<String> testSubscriber = new TestSubscriber<>();

        new ObservableBuilder<String>()
                    .emit("Glork")
                    .emit("flork")
                    .emit("fork")
                    .emit("spoon")
                    .sleep(1000)
                    .error(new Exception("There is no spoon"))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(testSubscriber);

        testSubscriber.awaitEvent(isErrorThat(hasMessageThat(containsString("no spoon"))));

        testSubscriber.assertDoesNotHaveEvent(isError(IOException.class));
        testSubscriber.assertHasEvent(isValue("spoon"));
        testSubscriber.assertWellBehaved();

        testSubscriber.beginAssertionChain()
                      .assertNextEvent(isValue("Glork"))
                      .assertNextEvent(isValueThat(containsString("ork")))
                      .assertNextEvent(isValueThat(endsWith("k")))
                      .assertNextEvent(isValue("spoon"))
                      .assertNextEvent(isErrorThat(hasMessageThat(containsString("no spoon"))));
    }
```

Failures look like thiis
```
java.lang.AssertionError: 
   Expected: onNext with value matching: is "knife"
        but: was onNext("spoon")
event chain: 
             onNext("Glork")
             onNext("flork")
             onNext("fork")
    -------> onNext("spoon")
             onError(<java.lang.Exception: There is no spoon>)
```

## Usage
Gradle
```Groovy
repositories {
    jcenter()
}
dependencies {
    compile 'reactive-caffeine:reactive-caffeine-testing:0.0.1'
}
```
Maven
```XML
<dependency>
  <groupId>reactive-caffeine</groupId>
  <artifactId>reactive-caffeine-testing</artifactId>
  <version>0.0.1</version>
</dependency>
```

ReactiveCaffeineTesting should work just fine on Java 7, or Android from API levels 8 and up
