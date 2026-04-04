# Email Finder - Java Library

[![Maven Central](https://img.shields.io/maven-central/v/io.enrow/email-finder.svg)](https://central.sonatype.com/artifact/io.enrow/email-finder)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)

Find verified professional email addresses from a name and company. Integrate email discovery into your sales pipeline, CRM sync, or lead generation workflow.

Powered by [Enrow](https://enrow.io) — works on catch-all domains, only charged when an email is found.

## Installation

**Maven**

```xml
<dependency>
    <groupId>io.enrow</groupId>
    <artifactId>email-finder</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle**

```groovy
implementation 'io.enrow:email-finder:1.0.0'
```

Requires Java 11+. Only dependency: Gson.

## Simple Usage

```java
import io.enrow.emailfinder.EmailFinder;
import java.util.Map;

Map<String, Object> search = EmailFinder.find("your_api_key", Map.of(
    "fullname", "Tim Cook",
    "company_domain", "apple.com"
));

Map<String, Object> result = EmailFinder.get("your_api_key", (String) search.get("id"));

System.out.println(result.get("email"));         // tcook@apple.com
System.out.println(result.get("qualification")); // valid
```

`EmailFinder.find` returns a search ID. The search runs asynchronously — call `EmailFinder.get` to retrieve the result once it is ready. You can also pass a `webhook` URL inside a `settings` map to get notified automatically.

## Search by company name

If you don't have the domain, you can search by company name instead. Pass a `country_code` inside `settings` to narrow down results when company names are ambiguous.

```java
Map<String, Object> search = EmailFinder.find("your_api_key", Map.of(
    "fullname", "Tim Cook",
    "company_name", "Apple Inc.",
    "settings", Map.of("country_code", "US")
));
```

## Bulk search

```java
Map<String, Object> batch = EmailFinder.findBulk("your_api_key", Map.of(
    "searches", java.util.List.of(
        Map.of("fullname", "Tim Cook", "company_domain", "apple.com"),
        Map.of("fullname", "Satya Nadella", "company_domain", "microsoft.com"),
        Map.of("fullname", "Jensen Huang", "company_name", "NVIDIA")
    )
));

// batch.get("batchId"), batch.get("total"), batch.get("status")

Map<String, Object> results = EmailFinder.getBulk("your_api_key", (String) batch.get("batchId"));
// results.get("results") — list of result maps
```

Up to 5,000 searches per batch. Pass a `webhook` URL inside `settings` to get notified when the batch completes.

## Error handling

```java
try {
    EmailFinder.find("bad_key", Map.of("fullname", "Test", "company_domain", "test.com"));
} catch (RuntimeException e) {
    // e.getMessage() contains the API error description
    // Common errors:
    // - "Invalid or missing API key" (401)
    // - "Your credit balance is insufficient." (402)
    // - "Rate limit exceeded" (429)
}
```

## Getting an API key

Register at [app.enrow.io](https://app.enrow.io) to get your API key. You get **50 free credits** (= 50 emails) with no credit card required.

Paid plans start at **$17/mo** for 1,000 emails up to **$497/mo** for 100,000 emails. See [pricing](https://enrow.io/pricing).

## Documentation

- [Enrow API documentation](https://docs.enrow.io)
- [Full Enrow SDK](https://github.com/enrow/enrow-java) — includes email verifier, phone finder, reverse email lookup, and more

## License

MIT — see [LICENSE](LICENSE) for details.
