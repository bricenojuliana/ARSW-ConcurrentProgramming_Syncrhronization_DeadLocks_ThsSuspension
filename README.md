
## Escuela Colombiana de Ingeniería
### Arquitecturas de Software – ARSW


#### Ejercicio – programación concurrente, condiciones de carrera y sincronización de hilos. EJERCICIO INDIVIDUAL O EN PAREJAS.

##### Parte I – Antes de terminar la clase.

Control de hilos con wait/notify. Productor/consumidor.

1. Revise el funcionamiento del programa y ejecútelo. Mientras esto ocurren, ejecute jVisualVM y revise el consumo de CPU del proceso correspondiente. A qué se debe este consumo?, cual es la clase responsable?

    ![image](https://github.com/user-attachments/assets/690666d4-149b-4f1c-9b87-4f7a2cf8f7f7)
   
   El consumo de CPU en el programa se debe principalmente a la implementación del Consumer en un bucle sin ninguna forma de espera, lo que resulta en un uso continuo de la CPU. Consumer está en una espera activa y haciendo comprobaciones constantemente, mientras que Producer añade un nuevo elemento a la cola cada segundo, lo que significa que deja descansar la CPU durante un segundo cada vez.

3. Haga los ajustes necesarios para que la solución use más eficientemente la CPU, teniendo en cuenta que -por ahora- la producción es lenta y el consumo es rápido. Verifique con JVisualVM que el consumo de CPU se reduzca.

    ![image](https://github.com/user-attachments/assets/391c2e46-7d58-4bfa-9ae6-0af5d2522995)
   
   El consumo de CPU pasó de 12% a casi 0%. Lo redujimos usando los métodos de la clase LinkedBlockingQueue<E> put() y take(), que ya funcionan para esperar hasta que haya espacio o la lista no este vacía respectivamente, que por debajo lo que sucede es un juego con wait() y notifyAll(), donde los consumidores preguntan a la cola si está vacía y si es así se duermen, mientras que el productor cada vez que añade un elemento despierta a los consumidores para avisar que ya no está vacía la cola.
   
5. Haga que ahora el productor produzca muy rápido, y el consumidor consuma lento. Teniendo en cuenta que el productor conoce un límite de Stock (cuantos elementos debería tener, a lo sumo en la cola), haga que dicho límite se respete. Revise el API de la colección usada como cola para ver cómo garantizar que dicho límite no se supere. Verifique que, al poner un límite pequeño para el 'stock', no haya consumo alto de CPU ni errores.

   ![image](https://github.com/user-attachments/assets/07e40a69-a7be-42b1-a35c-87c750fddbc7)
   
   Poniendo un límite de 10 elementos, imprimimos el tamaño de la lista después de agregar un elemento, y vemos que el tamaño de la cola no supera este límite. Para que el productor produzca rápido le quitamos la esperade un segundo que tenía y para que el consumidor consuma más lento le pusimos una espera de un segundo.

   ![image](https://github.com/user-attachments/assets/16abe024-0612-41a6-a9ae-f7c240437121)


   
##### Parte II. – Antes de terminar la clase.

Teniendo en cuenta los conceptos vistos de condición de carrera y sincronización, haga una nueva versión -más eficiente- del ejercicio anterior (el buscador de listas negras). En la versión actual, cada hilo se encarga de revisar el host en la totalidad del subconjunto de servidores que le corresponde, de manera que en conjunto se están explorando la totalidad de servidores. Teniendo esto en cuenta, haga que:

- La búsqueda distribuida se detenga (deje de buscar en las listas negras restantes) y retorne la respuesta apenas, en su conjunto, los hilos hayan detectado el número de ocurrencias requerido que determina si un host es confiable o no (_BLACK_LIST_ALARM_COUNT_).
- Lo anterior, garantizando que no se den condiciones de carrera.

##### Parte III. – Avance para el martes, antes de clase.

Sincronización y Dead-Locks.

![](http://files.explosm.net/comics/Matt/Bummed-forever.png)

1. Revise el programa “highlander-simulator”, dispuesto en el paquete edu.eci.arsw.highlandersim. Este es un juego en el que:

	* Se tienen N jugadores inmortales.
	* Cada jugador conoce a los N-1 jugador restantes.
	* Cada jugador, permanentemente, ataca a algún otro inmortal. El que primero ataca le resta M puntos de vida a su contrincante, y aumenta en esta misma cantidad sus propios puntos de vida.
	* El juego podría nunca tener un único ganador. Lo más probable es que al final sólo queden dos, peleando indefinidamente quitando y sumando puntos de vida.

2. Revise el código e identifique cómo se implemento la funcionalidad antes indicada. Dada la intención del juego, un invariante debería ser que la sumatoria de los puntos de vida de todos los jugadores siempre sea el mismo(claro está, en un instante de tiempo en el que no esté en proceso una operación de incremento/reducción de tiempo). Para este caso, para N jugadores, cual debería ser este valor?.
Teniendo en cuenta el sistema de obtención y perdida de puntos de vida, el valor para N jugadores deberia ser el resultado de multiplicar la constante DEFAULT_IMMORTAL_HEALTH * N jugadores.

3. Ejecute la aplicación y verifique cómo funcionan las opción ‘pause and check’. Se cumple el invariante?.

No se está cumpliendo el invariante ya que en cada pause and check se registran diferentes valores de vida, dando a entender que se estan ganando mas puntos de vida que los que se pierden

![image](https://github.com/user-attachments/assets/34148545-fd6b-4d80-a7e4-2f182bdf7040)
![image](https://github.com/user-attachments/assets/e8409881-7b0e-4c12-b4c4-f4e876534dc5)
![image](https://github.com/user-attachments/assets/641d45d1-81a3-4026-a9a2-5c528e5d837c)


4. Una primera hipótesis para que se presente la condición de carrera para dicha función (pause and check), es que el programa consulta la lista cuyos valores va a imprimir, a la vez que otros hilos modifican sus valores. Para corregir esto, haga lo que sea necesario para que efectivamente, antes de imprimir los resultados actuales, se pausen todos los demás hilos. Adicionalmente, implemente la opción ‘resume’.

5. Verifique nuevamente el funcionamiento (haga clic muchas veces en el botón). Se cumple o no el invariante?.

![image](https://github.com/user-attachments/assets/6df788fd-820d-4c60-af67-cb883e602ed9)
![image](https://github.com/user-attachments/assets/473f319e-d6ee-4aaf-9200-7115a901d973)

El invariante sigue sin cumplirse correctamente, ya que no solo es necesario parar los hilos. Siguen presentandose condiciones de carrera

6. Identifique posibles regiones críticas en lo que respecta a la pelea de los inmortales. Implemente una estrategia de bloqueo que evite las condiciones de carrera. Recuerde que si usted requiere usar dos o más ‘locks’ simultáneamente, puede usar bloques sincronizados anidados:

	```java
	synchronized(locka){
		synchronized(lockb){
			…
		}
	}
	```
Identificamos como region critica el metodo fight, debido a que aqui se consultan y cambian los valores de dos inmortales, lo que genera condiciones de carrera

![image](https://github.com/user-attachments/assets/9a71c38d-27b6-41ff-b901-e7db218c3a4b)

Nuestra estrategia implementa es bloquear los dos objetos que se están modificando, el primero sería yo que soy el que estoy atacando, y el otro a quien estoy atacando.

7. Tras implementar su estrategia, ponga a correr su programa, y ponga atención a si éste se llega a detener. Si es así, use los programas jps y jstack para identificar por qué el programa se detuvo.

Usamos el comando jps -l para listar todos los procesos ejectuandose en JVM, aqui identificamos el número del proceso perteneciente a la simulación

![image](https://github.com/user-attachments/assets/c77b67ba-601b-447f-afa6-aa85c00585ad)

Una vez obtenido este id, usamos el comando jstack para obtener información sobre el estado de los hilos ejectudandose en el proceso

![image](https://github.com/user-attachments/assets/4aa59a4e-e184-46d3-9f11-745d935dc4b6)

En la última línea podemos ver que se encuentran en un estado de Deadlock

![image](https://github.com/user-attachments/assets/32bd8400-61be-4249-9f90-31c4af1010c5)



8. Plantee una estrategia para corregir el problema antes identificado (puede revisar de nuevo las páginas 206 y 207 de _Java Concurrency in Practice_).

Planteamos ordenar los hilos por el indice que ocupan dentro de la lista de inmortales.

![image](https://github.com/user-attachments/assets/9977bcfd-34d2-47c5-9d14-3e22e32f74d6)

Con esta solución se cumple el invariante para 3 inmortales

![image](https://github.com/user-attachments/assets/390edec7-f005-4fc3-a49a-be4b23fcd734)

![image](https://github.com/user-attachments/assets/48ccecce-4825-4efb-9b4e-f8ed44e0eb7e)



9. Una vez corregido el problema, rectifique que el programa siga funcionando de manera consistente cuando se ejecutan 100, 1000 o 10000 inmortales. Si en estos casos grandes se empieza a incumplir de nuevo el invariante, debe analizar lo realizado en el paso 4.

Comprobamos en el caso de 100:

![image](https://github.com/user-attachments/assets/9f1a96cf-d1c8-45d9-a13c-88d1a45b75c0)

Comprobamos en el caso de 1000:

![image](https://github.com/user-attachments/assets/baafbde2-a9b2-404a-b1e8-4e5f759452ec)

Comprobamos en el caso de 10000:

![image](https://github.com/user-attachments/assets/5d8e602b-2c59-4272-81a4-328959d43c72)

(notamos que con esta cantidad de hilos se demorá mucho en empezar a ejecutar el proceso, una vez empieza el pause and check y resume funcionan correctamente)

10. Un elemento molesto para la simulación es que en cierto punto de la misma hay pocos 'inmortales' vivos realizando peleas fallidas con 'inmortales' ya muertos. Es necesario ir suprimiendo los inmortales muertos de la simulación a medida que van muriendo. Para esto:
	* Analizando el esquema de funcionamiento de la simulación, esto podría crear una condición de carrera? Implemente la funcionalidad, ejecute la simulación y observe qué problema se presenta cuando hay muchos 'inmortales' en la misma. Escriba sus conclusiones al respecto en el archivo RESPUESTAS.txt.
	* Corrija el problema anterior __SIN hacer uso de sincronización__, pues volver secuencial el acceso a la lista compartida de inmortales haría extremadamente lenta la simulación.

11. Para finalizar, implemente la opción STOP.

Para la implementación de STOP simplemente tuvimos en cuenta reiniciar el estado de las variables a su estado original, además de detener la ejecución de los hilos con una condición booleana dentro del while de ejecución, y deshabilitamos la opción de detener la simulación si se encuentra pausada ya que los hilos están en espera

Versión final:
	-Al iniciar la simulación
 
![image](https://github.com/user-attachments/assets/8eceb312-565b-47de-be09-5e821730b89c)

	-Al darle a START:
 
 ![image](https://github.com/user-attachments/assets/d7897180-c938-427d-a623-c4507dbb964e)

	-Al darle al STOP:
 
![image](https://github.com/user-attachments/assets/ceaae5fb-ff8e-485f-a4c4-197b066a4be9)

<!--
### Criterios de evaluación

1. Parte I.
	* Funcional: La simulación de producción/consumidor se ejecuta eficientemente (sin esperas activas).

2. Parte II. (Retomando el laboratorio 1)
	* Se modificó el ejercicio anterior para que los hilos llevaran conjuntamente (compartido) el número de ocurrencias encontradas, y se finalizaran y retornaran el valor en cuanto dicho número de ocurrencias fuera el esperado.
	* Se garantiza que no se den condiciones de carrera modificando el acceso concurrente al valor compartido (número de ocurrencias).


2. Parte III.
	* Diseño:
		- Coordinación de hilos:
			* Para pausar la pelea, se debe lograr que el hilo principal induzca a los otros a que se suspendan a sí mismos. Se debe también tener en cuenta que sólo se debe mostrar la sumatoria de los puntos de vida cuando se asegure que todos los hilos han sido suspendidos.
			* Si para lo anterior se recorre a todo el conjunto de hilos para ver su estado, se evalúa como R, por ser muy ineficiente.
			* Si para lo anterior los hilos manipulan un contador concurrentemente, pero lo hacen sin tener en cuenta que el incremento de un contador no es una operación atómica -es decir, que puede causar una condición de carrera- , se evalúa como R. En este caso se debería sincronizar el acceso, o usar tipos atómicos como AtomicInteger).

		- Consistencia ante la concurrencia
			* Para garantizar la consistencia en la pelea entre dos inmortales, se debe sincronizar el acceso a cualquier otra pelea que involucre a uno, al otro, o a los dos simultáneamente:
			* En los bloques anidados de sincronización requeridos para lo anterior, se debe garantizar que si los mismos locks son usados en dos peleas simultánemante, éstos será usados en el mismo orden para evitar deadlocks.
			* En caso de sincronizar el acceso a la pelea con un LOCK común, se evaluará como M, pues esto hace secuencial todas las peleas.
			* La lista de inmortales debe reducirse en la medida que éstos mueran, pero esta operación debe realizarse SIN sincronización, sino haciendo uso de una colección concurrente (no bloqueante).

	

	* Funcionalidad:
		* Se cumple con el invariante al usar la aplicación con 10, 100 o 1000 hilos.
		* La aplicación puede reanudar y finalizar(stop) su ejecución.
		
		-->

<a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-nc/4.0/88x31.png" /></a><br />Este contenido hace parte del curso Arquitecturas de Software del programa de Ingeniería de Sistemas de la Escuela Colombiana de Ingeniería, y está licenciado como <a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/">Creative Commons Attribution-NonCommercial 4.0 International License</a>.
