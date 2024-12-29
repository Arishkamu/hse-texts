### Интерфейсы

Интерфейсы примерно как в Java. Есть реализация по умолчанию 

Статических методов в прямом смысле нет, но есть `companion`
* К нему можно обращаться, как будто это класс со статик полями

Варианты использования\
Есть несколько реализаций. В компаньон помещаем factory-метод, который по параметрам определяет подходящую реализацию

В интерфейсе могут встречаться свойства Но их нельзя материализовать\
Но можно определить get-метод (как метод по умолчанию)

```kotlin
interface Base1 { 
    val url: String = "https://www.yandex.ru" // так нельзя
}

interface Base2 { 
    val url: String 
        get() = field.substring(1) // и так тоже (нужен field)
}

interface Base3 {
    val url: String
        get() = "https://www.yandex.ru" // А так - можно
}
```

Может быть и параметр по умолчанию
```kotlin
interface Base {
    fun createUrl(host: String="www.yandex.ru"): String =
        "https://${host}"
}
object O : Base {
    override fun createUrl(host: String) = "ftp://${host}"
//    здесь нельзя написать свое значение host по умолчанию.
}
```

Как это реализовано: форме без параметра соответствует синтетический статический интерфейсный метод
* Ему передается параметром объект
* Он вызывает над объектом его реализацию метода
* И передает значение по умолчанию


Можно обратиться к конкретному методу по умолчанию через super<MyInterface>\
Помогает в разрешении неоднозначностей или в вызове "скрытого" метода

### Наследование
Финальность
* По умолчанию классы финальны, методы классов - тоже.\
* Методы интерфейсов - нет (было бы странно). Ключевое слово `open` отменяет финальность
* override подразумевает open
* Но иногда хочется положить этому конец. Можно перед override указать final


Доступ
* public доступность везде
* По умолчанию все - public
* protected - видимость в только наследниках
* Вне класса protected нет
* private-элемент класса виден только в классе private-элемент файла - только в файле 
* Внешний класс не видит private-элементы своих внутренних классов
* Но JVM не проверяет - поэтому можно обходить


Internal
* Обозначает видимость в рамках единицы сборки (модуля)
* Например, собираем библиотеку: класс нужен много где в библиотеке, но он служебный


По умолчанию внутринний класс - статический, меняется ключевым словом inner
```kotlin
class C {
    private inner class C2 {
        val v: String = vv
    }

    private val c = C2()
    private val vv = "hello"
    val vvv: String
        get() = c.v
}
fun main () {
    println(C().vvv) // null. Хуже он не конструированный
}
```


Sealed-класс\
Мотивация: создать ограниченное/заранее определенное число наследников
* Хотим убедить компилятор в том, что других подклассов нет
* Все подклассы должны быть в том же файле 
* Нельзя анонимный package
```kotlin
sealed class Expr { 
    class Value(val v: Int) : Expr()
    class Sum(val e1: Expr, val e2: Expr) : Expr()
}
fun eval(e: Expr): Int = when (e) {
    is Expr.Value -> e.v
    is Expr.Sum -> eval(e.e1) + eval(e.e2)
//    не нужен else
}
```


### Конструктор суперкласса
* Связь задается при объявлении класса Не в описании конструктора
* Страдает single-responsibility
* Меньше вариантов стыковки
* Вторичные связываются с суперклассом через первичного
```kotlin
class Parent(val name: String) // Суперкласс с primary-конструктором

class Child(name: String, val age: Int) : Parent(name) // Вызываем конструктор Parent
```


### Исключения
* Как в java. 
* `try` - выражение те его можно присваивать и тд. 
* Нет понятия 'checked exception' (нет перечня exception у методов)


* `try-with-resources` в явном виде нет, потому что это умножение сущностей 
* Есть метод `use`, как расширение `Closeable`
```kotlin 
inline fun <T : Closeable?, R> T.use(block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    var exception: Throwable? = null
    try {
        return block(this)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        when {
            apiVersionIsAtLeast(1, 1, 0) -> this.closeFinally(exception)
            this == null -> {}
            exception == null -> close()
            else ->
                try {
                    close()
                } catch (exc: Throwable) { //cause.addSuppressed(exc)-tokeeplegacybehaiviour }
                }
        }
    }
}
```


with/apply\
Хотим получить строку через StringBuilder. Можно в лоб завести переменную-билдер, что-то с ней поделать и вернуть результат

Kotlin позволяет сделать красивее
```kotlin
fun alphabet() = with(StringBuilder()) { 
    for (letter in 'A'..'Z') { 
        append(letter)
    }
    append("\nNow I know the alphabet!")
    this.toString()
}
// примерно как везде перед написать
// val res = StringBuilder() 
// а потом res.append(); res.toString()
```


Изнутри
```kotlin
public inline fun <T, R> with(
    receiver: T, 
    block: T.() -> R
): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return receiver.block()
}
```

`T.(A) -> R` - lambda with reciver\
По сути лямбда с расширением. То есть у типа T появилась лямбда (расширили тип T).\
Можно понимать как лямбду (this: T, A) -> R\
Разделяем монолит на переменную и действия, преобразовывать их


**Apply**
* Близкий аналог with
* Метод, а не функция
* Возвращает this
*  Вариант использования: быстрая отладка


Лямбда и замыкание внутри функций
* Изменять состояние переменной не можем, тк значение неизменяемой переменной примитивного типа можно добавить в замыкание
* А с адресом - сложнее, он на стеке
* А объект - можно. Он в куче
* Даже если не меняем надо 'effective final'

В котлине
* Под капотом - примерно то, что сделано в Java-примере
* Только тип - не атомарный: класс называется IntRef
* Можно даже использовать явным образом
* Не надо 'effective final' тк по умолчанию val. Через var можно, но не надо