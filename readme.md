# Hanayo

![](res/img/logo.png)

**Hanayo is finished but not tested. Contribute by submitting test cases!**

Hanayo is an experimental HOCON parser written in plain Java for exercising and school homework.

HOCON is a human-readable config format and a superset of JSON. Specs: [HOCON Spec][spec] (that repo also hosts the official java implementation).

[spec]: https://github.com/lightbend/config/blob/master/HOCON.md

## Develop

Hanayo requires at least Java 11 to compile and run.

## Feature list

Parser completion status for language grammars are listed below (see [this file][grammar] for the full language grammar):

- [x] EOL
- [x] Whitespace
- [x] EscapedSequence
- [x] QuotedString
- [x] UnquotedString
- [x] MultilineString
- [x] PathSegment
- [x] Key
- [x] Number
- [ ] DurationUnit
- [ ] Duration
- [ ] SizeUnit
- [ ] Size
- [x] DeterminedSubstitution
- [x] NonDeterminedSubstitution
- [x] Boolean
- [x] Substitution
- [x] ValueSegment
- [x] Value
- [x] Separator
- [x] KeyValuePair
- [x] ElementSeparator
- [x] KeyValuePairs
- [x] Map
- [x] Values
- [x] List
- [x] Document

[grammar]: https://github.com/01010101lzy/hanayo/blob/master/docs/hocon-language.txt

## Notes

To make coding easier, Hanayo only implements a subset of HOCON. Its difference from the standard HOCON format are listed below (the list may grow):

- Consecutive dots in keys (_e.g. `path1..path2`_) are not considered as format error. They evaluate into empty path segments.
- Hanayo uses an "early replace" strategy of value concatenation, opposing to the "read all then replace" strategy requested in the spec.
- Includes are not implemented.
- Duration and sizes are not yet implemented.

## Licensing

Hanayo is released under MIT license.

---

You guessed it. It's Koizumi Hanayo.
