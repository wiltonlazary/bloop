## Prototype of Runner protocol

When Bloop wants to run an application, it follows the following steps:

1. It starts a runner server if there isn't one running.
2. It detects what changes have happened in the classpath since the last run.
3. It assembles a 

Challenges:

1. What do we do if the client application is still running when the user wants to run it again? How do we shut it down properly?
2. What do we do if the client application does `System.exit` or mutates system properties?
3. Is there a risk that the client application has access to Nailgun's origin input/output streams?

### Description

The JVM runner protocol allows build tools (e.g. clients) to manage the state
and class loaders of a JVM (e.g. server) that can run applications with
built-in classloader caching.

Built-in classloader caching is a technique to mitigate the slow down of
starting a new JVM every time a main class is run. By reusing a running JVM
and those class loaders whose classes haven't changed across runs, the host
JVM can reduce startup load and run the application faster. In short,
built-in classloader caching allows to reuse the "hot" state of previous
executions.

The protocol was born to let clients manage that class loader state based on
external data. For example, build tools often know before they have actually
run an application which classpath entries have changed and the host JVM can
reuse this information to directly invalidate a class loader that contains
those entries, without incurring on IO-bound checks to prove the contents on
the file system are the same.

The JVM runner protocol allows configuring which classpath entries should be
mapped to each "class loader layer". When any of the classpath entries has
been changed, the host JVM invalidates that layer. An execution of a class
can reuse several class loader layers and when a layer is invalidated it is
recreated from scratch.

The protocol is configurable so that the client asking for the execution
decides how many class loader layers are reused when running a JVM main class
should be run.

### How does class loader invalidation work?

When a class loader layer is invalidated, the host JVM marks it as dirty so
that the next execution of an application loads classes affected by that
layer again. On top of that, the invalidation `close`s the class loader so
that the garbage collection can successfully clean up all the state
associated with that class loader.

### Can we run any application via the JVM runner protocol?

There are certain kinds of applications that you can run via the JVM runner
protocol but that might have unexpected behaviors. This can happen if:

1. The application stores state globally that takes up heap/stack space.
2. The application manages class loaders and has class loader leaks.
3. The application is not cooperative and doesn't exit successfully.
4. The application assumes it's running in a terminal.
5. The applications assumes it's running in a specific environment. For
   example, it assumes running in a terminal, or having concrete
   `System.in`/`System.out`.

How do we kill an application that hasn't exited yet?

- Kill the thread where it's running?
- Kill the thread pool where it's running?

### Where should I enable built-in class loader caching?

Despite some of its limitations, class loader caching is often used to
achieve a tight edit/compile/test experience. In these scenarios, people are
editing code locally and are happy to trade the speed of the test execution
with the potential misbehaviors that can happen from time to time.
