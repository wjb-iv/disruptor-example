# LMAX Disruptor Ring-Buffer Example

An example of using the LMAX disruptor framework:
> http://lmax-exchange.github.io/disruptor/ 
 
Inspired by this blog post:
> http://blog.jteam.nl/2011/07/20/processing-1m-tps-with-axon-framework-and-the-disruptor/
 
This is an extremely oversimplified version of the "diamond configuration" as described in:
> http://mechanitis.blogspot.com/2011/07/dissecting-disruptor-wiring-up.html

In this implementation, a journal and replication step happen concurrently, and both must succeed for the post step to occur, as shown below: 

```
          replicate
            /   \
           /     \
  event ->       post
           \     /
            \   /
           journal
```
 
This is also an example of in-memory storage based on "event sourcing" - see Martin Fowler: 
> http://martinfowler.com/eaaDev/EventSourcing.html
