# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A small teaching project (Spring Boot 4.1 / Java 25 / Maven) demonstrating the **monitor synchronization construct** in Java: N consumer threads compete for a `synchronized` singleton storage of integers, a notifier thread `wait()`s until the storage is empty, and a Swing window observes everything via `PropertyChangeListener`. The README is the primary narrative documentation and contains a Mermaid class diagram; keep it in sync when changing public class shapes.

## Commands

Use the Maven wrapper (`./mvnw`); no local Maven install is required. JDK 25 is required (see `.sdkmanrc`).

```bash
./mvnw spring-boot:run                          # run the Swing app (opens a window, needs a display)
./mvnw test                                     # run all tests + generate JaCoCo report
./mvnw test -Dtest=IntegerStorageMonitorImplTests                          # single test class
./mvnw test -Dtest=IntegerStorageMonitorImplTests#consumeIntDecrementsUntilZero  # single method
./mvnw verify                                   # what CircleCI runs
./mvnw -B package                               # what GitHub Actions runs
```

JaCoCo HTML report lands in `target/site/jacoco/index.html`. The `jacoco:check` goal runs in the `test` phase and fails the build below 95% instruction / 90% branch coverage; on top of that the GitHub Actions workflow regenerates `.github/badges/jacoco.svg` and commits it back to `master` on every push. The `ui/**`, `Constants`, and the `@SpringBootApplication` class are excluded from coverage in `pom.xml`, so tests are expected to cover everything else (recent history pushed it to 100%).

## Architecture

All code lives under `com.penapereira.example.javamonitor`.

**Wiring / lifecycle**
- `JavaThreadsMonitorExampleApplication.main` boots Spring with `headless(false)`, then on the AWT `EventQueue` calls `launchSimulation()`. Spring is used only as a launcher; there are no beans besides the application class and no `application.properties` content.
- `SimulationController` (singleton via `instance()`) is the composition root. `initialize(UIManager)` creates the storage monitor, starts the notifier thread, activates the UI, registers the UI as listener on notifier and every consumer, then starts one `Thread` per `IntegerConsumerImpl`. `startSimulation()` shows the UI's greeting dialog and then flips the monitor's `started` flag, which releases all consumers blocked in `consumeInt()`. `stopSimulation()` detaches the UI listener, terminates, interrupts, force-stops and joins every thread (in that order; interrupt must precede the `synchronized` `forceStop()` or the caller queues behind sleeping lock holders); `restartSimulation()` runs that, calls `IntegerStorageMonitorImpl.reset()` and `UIManager.reset()`, relaunches, and starts immediately without the greeting. The `Monitor → Restart` menu reaches it through a `Runnable` passed to `UIManagerSwingImpl`'s constructor by the application class; the `ui` package must never import `SimulationController`.
- Defaults (9 consumers, 15 integers, 500 ms step, and the property-name strings `stop`/`consumed`/`finished`) are in `Constants`.

**The monitor (`monitor` package)**
- `IntegerStorageMonitorImpl` is the singleton monitor: every public method is `synchronized`. `consumeInt()` blocks on `wait()` until `started`, sleeps `waitMillis`, calls `notifyAll()`, and decrements. `waitForAllIntegersToBeConsumed()` loops on `wait()` until the count hits zero. `forceStop()` sets a flag and `notifyAll()`s; waiters then throw `ForcedStopException`. Note the singleton's `instance(consumableInts, waitMillis)` ignores its arguments after first creation; the static `reset()` discards it, which a restart needs because `forceStop` is one-way.
- `Observable` / `AbstractObservable` wrap `PropertyChangeSupport`. Subclasses fire events through `getSupport()` and call `removeAllListeners()` in their `run()` `finally` block.
- `EmptyIntegerStorageNotifier` is a `Runnable` observable that fires `finished` once the storage drains.

**Consumers (`consumer` package)**
- `IntegerConsumer` extends `Runnable` and `Observable`. `IntegerConsumerImpl.run()` loops `while (running && monitor.hasIntegers())`, firing `consumed` with the new value; `terminate()` clears `running`. A `ForcedStopException` fires `stop`.

**UI (`ui` package)**
- `UIManager` extends `PropertyChangeListener` and adds `activate()` / `startSimulation()` / `reset()`. `UIManagerSwingImpl` builds a 3x3 grid of random-colored panels indexed by consumer id and reacts to the `consumed` and `finished` property names. It is excluded from coverage and tests never touch Swing; use a fake `UIManager` in tests (see `SimulationControllerTests.DummyUI`).

## Testing conventions

- JUnit 6 (Jupiter) via `spring-boot-starter-test`; plain unit tests, no Mockito (`mockito-core` is excluded from the starter in `pom.xml`; add it back before mocking anything). Only `JavaThreadsMonitorExampleApplicationTests` is a `@SpringBootTest`.
- Both singletons hold static state (`SimulationController._uniqueInstance`, `IntegerStorageMonitorImpl._instance`). Tests reset the controller via reflection and the monitor via `IntegerStorageMonitorImpl.reset()` in `@BeforeEach`; `SimulationControllerTests` also stops and joins leftover threads in `@AfterEach`. Do the same in any new test that touches them, or tests will leak state and live threads across classes.
- `IntegerStorageMonitorImpl`'s constructor is `protected` and the test is in the same package, so tests instantiate it directly with `waitMillis = 0` to avoid sleeping.
- Thread-based tests use short `Thread.sleep` + `join(timeout)` rather than latches; keep new ones fast.

## Repo hygiene

- `.classpath`, `.project`, and `.settings/` are Eclipse/STS artifacts that are gitignored but were committed historically; they show as modified locally. Do not stage them.
- Branch naming in use: `codex/<topic>`; PRs target `master`.
