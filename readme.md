# Hanayo

![](res/img/logo.png)

**Hanayo is not yet finished.**

Hanayo is an experimental HOCON parser written in plain Java for exercising and school homework.

HOCON is a human-readable config format and a superset of JSON. Specs: [HOCON Spec][spec] (that repo also hosts the official java implementation).

[spec]: https://github.com/lightbend/config/blob/master/HOCON.md

## Develop

Hanayo requires at least Java 11 to compile and run.

## Notes

To make coding easier, Hanayo only implements a subset of HOCON. Its difference from the standard HOCON format are listed below (the list may grow):

- Consecutive dots in keys (_e.g. `path1..path2`_) are not considered as format error. They evaluate into empty path segments.
- Hanayo uses an "early replace" strategy of value concatenation, opposing to the "read all then replace" strategy requested in the spec.
- Includes are not implemented.
- Duration and sizes are not implemented.

## Licensing

Hanayo is released under MIT license.

---

You guessed it. It's Koizumi Hanayo.
