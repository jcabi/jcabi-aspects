<img src="http://img.jcabi.com/logo.png" width="200px" height="48px" />

More details are here: [www.jcabi.com/jcabi-aspects](http://www.jcabi.com/jcabi-aspects/index.html)

This module contains a collection of useful AOP aspects, which
allow you to modify the behavior of a Java application without
writing a line of code. For example, you may want to retry HTTP
resource downloading in case of failure. You can implement a full
`do/while` cycle yourself, or you can annotate your method with
`@RetryOnFailure` and let one of our AOP aspects do the work for you:

```java
public class MyResource {
  @RetryOnFailure
  public String load(URL url) {
    return url.openConnection().getContent();
  }
}
```

You need just this dependency:

```xml
<dependency>
  <groupId>com.jcabi</groupId>
  <artifactId>jcabi-aspects</artifactId>
  <version>0.13</version>
</dependency>
```

Full list of AOP annotations is [here](http://www.jcabi.com/jcabi-aspects/).

## Questions?

If you have any questions about the framework, or something doesn't work as expected,
please [submit an issue here](https://github.com/jcabi/jcabi-aspects/issues/new).
If you want to discuss, please use our [Google Group](https://groups.google.com/forum/#!forum/jcabi).

## How to contribute?

Fork the repository, make changes, submit a pull request.
We promise to review your changes same day and apply to
the `master` branch, if they look correct.

Please run Maven build before submitting a pull request:

```
$ mvn clean install -Pqulice
```
