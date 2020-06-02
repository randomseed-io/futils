# futils – Function Utilities Library

[![Futils on Clojars](https://img.shields.io/clojars/v/io.randomseed/futils.svg)](https://clojars.org/io.randomseed/futils)
[![CircleCI](https://circleci.com/gh/randomseed-io/futils.svg?style=svg)](https://circleci.com/gh/randomseed-io/futils)

This library defines forms that abstract some common operations on functions,
including counting arguments, creating wrappers, passing proper number of
arguments and transforming positional-based arities into named ones.

## Installation

The current release is 1.2.3. To use futils in your project, add the following
to the `dependencies` section of `project.clj`:

```
[io.randomseed/futils "1.2.3"]
```

## Components

Currently prvided macros and functions are:

* [`argc`][argc] – counts arguments a function takes (for all arities),
* [`apply`][apply] – works like `clojure.core/apply` but for named arguments,
* [`comp`][comp] – works like `clojure.core/comp` but for named arguments,
* [`frepeat`][frepeat] – creates a sequence of returned values using a function
  with named parameters,
* [`identity`][identity] – works like `clojure.core/identity` but for named arguments,
* [`nameize`][nameize] – transforms a function so it accepts named arguments,
* [`nameize*`][nameize*] – like `nameize` but requires symbols to be quoted,
* [`relax`][relax] – wraps a function in a way that it accepts any number of
  arguments,
* [`relax*`][relax*] – like `relax` but it requires to explicitly
  describe the accepted arities.

## Documentation

Full documentation with usage examples is available on:

* https://randomseed.io/software/futils/

## Sneak peeks

```clojure
(require 'futils.args)
(require 'futils.named)

;; counting arities
;;
(futils.args/argc reduce)
; => {:arities (2 3)
      :engine :jvm
      :variadic false}

;; relaxing arities
;;
(def f (futils.args/relax reduce))
(f + 0 [1 2 3 4] :ignored :args)
; => 10

(def f (futils.args/relax #(vector %1 %2)))
(f 1)
; => [1 nil]

(def f (futils.args/relax #(vector %1 %2) :verbose true))
(f 1)
; => {:argc-cutted 0
; =>  :argc-padded 1
; =>  :argc-received 1
; =>  :argc-sent 2
; =>  :args-received (1)
; =>  :args-sent (1 nil)
; =>  :arities (2)
; =>  :arity-matched 2
; =>  :engine :jvm
; =>  :result [1 nil]
; =>  :variadic false
; =>  :variadic-used false
; =>  :verbose true}

;; nameization
;;
(def f (futils.named/nameize
        reduce
        [f coll]
        [f val coll]))

(f :f +
   :coll [1 1 2 3])
; => 7

(f :f +
   :val 1
   :coll [1 1 2 3])
; => 8

;; function composition
;;
(defn f1 [& {:as args}] (assoc args :f1 1))
(defn f2 [& {:as args}] (assoc args :f2 2))
(def f (futils.named/comp f1 f2))

(f :a 1 :b 2)
; => {:a 1 :b 2 :f1 1 :f2 2}
```

## Examples

Examples can be found in the documentation or in test files located under
`test/futils` subdirectory of the sources.

## Development

### Building docs

```bash
make docs
```

### Building JAR

```bash
make jar
```

### Rebuilding POM

```bash
make pom
```

### Signing POM

```bash
make sig
```

### Deploying to Clojars

```bash
make deploy
```

### Interactive development

```bash
bin/repl
```

Starts REPL and nREPL server (port number is stored in `.nrepl-port`).

## License

Copyright © 2015-2020 Paweł Wilk

Futils is copyrighted software owned by Paweł Wilk (pw@gnu.org). You may
redistribute and/or modify this software as long as you comply with the terms of
the [GNU Lesser General Public License][LICENSE] (version 3).

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

[NEWS.md]:    https://github.com/siefca/futils/blob/master/NEWS.md
[LICENSE]:    https://github.com/siefca/futils/blob/master/LICENSE
[argc]:       https://randomseed.io/software/futils/#argc
[relax]:      https://randomseed.io/software/futils/#relax
[relax*]:     https://randomseed.io/software/futils/#relax*
[nameize]:    https://randomseed.io/software/futils/#nameize
[nameize*]:   https://randomseed.io/software/futils/#nameize*
[frepeat]:    https://randomseed.io/software/futils/#frepeat
[apply]:      https://randomseed.io/software/futils/#apply
[comp]:       https://randomseed.io/software/futils/#comp
[identity]:   https://randomseed.io/software/futils/#identity
