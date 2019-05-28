# Hanayo

Hanayo is an experimental HOCON parser written in plain Java for exercising and school homework.

HOCON is a human-readable config format. Specs: [HOCON Spec][spec] (that repo also hosts the official java implementation).

To make coding easier, some feature of HOCON were changed, as listed below:

- Hanayo uses an "early replace" strategy of value concatenation, opposing to the "read all then replace" strategy in the spec.
- Includes are not implemented in this parser.
- Duration and size formats are not implemented in this parser.

[spec]: https://github.com/lightbend/config/blob/master/HOCON.md

## Licensing

Hanayo is released under MIT license.
