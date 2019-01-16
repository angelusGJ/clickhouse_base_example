## 1. Introducción

A lo largo de la historia, la información y saber utilizarla ha sido y será clave para tomar decisiones en cualquier ámbito. Todo hemos oído frases como "la información es poder", el dato cada vez esta tomando mayor relevancia de la que ya tenía. Grandes compañías y no tan grandes ofrecen de forma gratuita sus servicios a cambio de nuestros datos, ¿por qué?, porque con los datos que recopilan de nosotros, leyendo nuestros mails, conociendo nuestros hábitos, nuestra salud, preferencias deportivas, etc ... generan un perfil que utilizan para ofrecernos publicidad orientada, productos en los que podemos estar interesados e incluso vender dicha información a terceros.

Business Intelligence es una de las áreas especializadas en la analítica del dato, se caracteriza en transformar información en conocimiento, con el objetivo de mejorar el proceso de tomas de decisiones haciendo uso de estrategias y herramientas. El volumen de la información puede llegar a millones de registros, esto hace que el uso de soluciones tradicionales no sean las más adecuadas.

Una de las estrategias más habituales en este área es OLAP (OnLine Analytical Processing) que permite agilizar la consulta de grandes cantidades de datos usando estructuras multidimensionales que contienen información resumida de otras fuentes de datos del tipo OLTP (Sistemas transaccionales). Su uso habitual es la generación de informes para los diferentes departamentos de una empresa como pueden ser ventas, marketing, etc.

Básicamente OLAP recopila un gran volumen de información de diversas fuentes (internas o externas), las agrupa de una forma determinada para poder después explotar la información accediendo a ella mediante el uso de un lenguaje más natural, como por ejemplo: "dime el número de billetes vendidos en el último cuatrimestre por país", "rango de edades por países que acceden a la aplicación".

La características principales cuando se trabaja con escenarios OLAP son:

- La mayoría de las peticiones son de lectura
- Generalmente los datos no son modificados
- Tablas con un gran número de columnas
- Las consultas procesan un gran número de filas (billones de filas por segundo) pero sólo sobre un subconjunto de columnas
- No son necesarias las transacciones y desde el punto de vista de consistencia del dato no es un requisito esencial.

Teniendo en cuenta como se trabaja con los datos en escenarios OLAP, herramientas tradicionales como pueden ser base de datos como MySQL, Oracle o PostgreSQL no son las más adecuadas ya que el rendimiento es bastante pobre. Son base de datos orientadas a fila, es decir, los datos son almacenados en este orden:

| Fila	| OrderID	  | Pagada	      | Nombre	            | Enviada	   | Fecha               |
| ----- | ----------- | ------------- | ------------------- | ------------ | ------------------- | 
| #0	| 89354350662 |	1             |	Investor Relations	| 1	           | 2016-05-18 05:19:20 |
| #1	| 90329509958 |	0	          | Contact us	        | 1	           | 2016-05-18 08:10:20 |
| #2	| 89953706054 |	1	          | Mission	            | 1	           | 2016-05-18 07:38:00 |
| #N	| ...	      | ...	          | ... 	            | ...	       | ...                 |

Para que los rendimientos sean realmente óptimos, generalmente, las herramientas OLAP estructuran los datos en columnas almacenándolos de esta otra forma:

|             |	                    |    	              |  	                |     |
| ----------- | ------------------- | ------------------- | ------------------- | --- |
| Fila        |	#0                  | #1	              | #2	                | #N  |
| OrderID     |	89354350662         | 90329509958         |	89953706054	        | ... |
| Pagada      |	1	                | 0 	              | 1	                | ... |
| Nombre	  | Investor Relations  | Contact us	      | Mission             | ... |
| Enviada     |	1                   | 1                   | 1                   | ... |
| Fecha       |	2016-05-18 05:19:20 | 2016-05-18 08:10:20 |	2016-05-18 07:38:00 | ... |


En estos casos los valores de diferentes columnas se almacenan por separado y los datos de la misma columna se almacenan juntos. De esta forma la recuperación se puede realizar en bloque y se puede procesar muchos más datos que con las base de datos orientadas a fila.

## 2. Entorno

Este tutorial está escrito usando el siguiente entorno:

- Hardware: MacBook Pro 15’ (3,1 GHz Intel Core i7, 16GB DDR3)
- Sistema operativo: macOS Mojave 10.14.2
- Versiones del software: 
  - Docker: 18.09.1
  - ClickHouse Server: 18.14.18
  - ClickHouse Client: 18.14.18
  - Java: 8
  - Spring Boot: 2.1.1.RELEASE

## 3. ClickHouse

ClickHouse es una de las muchas herramientas para trabajar con este tipo de escenarios, es un producto desarrollado por Yandex (que podemos decir que es el Google Ruso) para utilizarlo como base de su herramienta de Yandex Metrica ([Live Demo](https://metrica.yandex.com/dashboard?group=week&period=year&id=44147844)) que es una aplicación similar a Google Analitics . Siendo la tercera plataforma de análisis web más utilizada del mundo por detrás de Google y Facebook.

Si echamos un vistazo a los informes de rendimiento que lo comparan con otras herramientas, ClickHouse se posiciona en una buena posición estando por encima de muchas de ellas. Os dejo unos cuantos enlaces de dichos informes:

- [ClickHouse: New Open Source Columnar Database](https://www.percona.com/blog/2017/02/13/clickhouse-new-opensource-columnar-database/)
- [Column Store Database Benchmarks: MariaDB ColumnStore vs. Clickhouse vs. Apache Spark](https://www.percona.com/blog/2017/03/17/column-store-database-benchmarks-mariadb-columnstore-vs-clickhouse-vs-apache-spark/)
- [ClickHouse vs Amazon RedShift Benchmark](https://www.altinity.com/blog/2017/6/20/clickhouse-vs-redshift)

Las características base de este producto son:

- Una base de datos realmente orientada a columna, sin utilizar datos extras como pueden ser la longitud de una cadena de caracteres, sólo almacena los datos.
- Los datos se comprimen reduciendo el espacio y se almacenan en disco a diferencia de otras herramientas que sólo trabajan en memoria. Esto hace que el coste por GB sea bajo.
- Capaz de procesar en paralelo los datos exprimiendo las capacidades multicores de los servidores.
- Los datos son procesados utilizando vectores, mejorando la eficiencia de la CPU. ClickHouse hace uso del conjunto de instrucciones SSE 4.2 que permite trabajar con estas estructuras de datos y hace que este producto sea capaz de procesar billones de registros en muy poco tiempo. Esta característica hace que sólo pueda ser desplegado en procesadores que sean compatibles con este conjunto de instrucciones.
- Utiliza el lenguaje SQL para ejecución de las sentencias casi idéntico al estándar SQL.
- Permite actualizar los datos en tiempo real sin bloqueo gracias a la forma de estructurar internamente los datos.
- Proporciona indexación por clave primaria. ClickHouse ordena fisicamente los datos en base a su clave primaria y hace posible recuperar la información almacenada para un determinado valor o un rango de valores con muy baja latencia y pocos milisegundos.
- Capaz de ejecutar consultas en tiempo real sin necesidad de procesar los datos previamente, es decir, ClickHouse puede trabajar con millones de registros en bruto (sin realizar preprocesamientoo previo de los datos) consiguiendo ejecutar sentencias sin ninguna latencia
- Soporte de replicación e integridad de datos.

### 3.1 Tipos de tablas

Como hemos comentado anteriormente, ClickHouse es una base de datos y como cualquier base de datos la información se almacena en tablas para posteriormente realizar consultas sobre ellas. La peculiaridad de esta base de datos es que dispone de un gran abanico de tipología de tablas.

En este tutorial sólo las nombraremos sin entrar en detalle. El primer tipo que podemos destacar es la familia de tablas denominadas **MergeTree**, podemos decir que son el núcleo y las que más usaremos para explotar nuestros datos. Las características más importantes de esta familia de tablas son:

- tienen clave primaria y los datos están ordenados físicamente en disco. Por ejemplo, si la clave primaria es (OrderId, Date) los datos estarán ordenados por OrderId y dentro de cada OrderId estarán ordenados por Date. Además ClickHouse mantiene un indice disperso que permite encontrar los datos más rápidamente y un formato bastante ingenioso y que os recomiendo que profundicéis leyendo la documentación.
- los datos también se dividen en particiones mediante una clave de partición similar a la clave primaria. Esta se utiliza para crear particiones, lo que permite optimizar las consultas sólo leyendo los datos de una determinada partición. Indicar que ClickHouse creará un fichero físico independiente por cada partición creada.

A la hora de diseñar una tabla para posteriormente realizar consultas sobre ellas deberemos prestar bastante atención y elegir correctamente estas dos claves de lo contrario podremos tener problemas de rendimiento y por tanto una experiencia de usuario pobre.

Partiendo de esta característica base de esta familia luego cada tipo de tabla tiene su peculiaridad. A continuación os muestro la lista de tablas de esta familia:

- **ReplacingMergeTree**: su principal características es que elimina los datos duplicados con la misma clave primaria.
- **SummingMergeTree**: une todas las filas con la misma clave primaria en una sola fila realizando un resumen de las filas compactadas.
- **AggregatingMergeTree**: remplaza todas las filas con la misma clave primaria con una única fila aplicando las funciones de agregación utilizadas (ejemplo: sum, avg).
- **CollapsingMergeTree**: elimina asíncronamente todas las filas que contiene los mismos campos sin tener en cuenta un campo especial llamado Sign. ClickHouse mantendrá la fila donde el valor Sign sea 1 y eliminará aquellas cuyo valor sea -1.
- **VersionedCollapsingMergeTree**: tipo de tabla similar a CollapsingMergeTree pero añadiendo un campo adicional llamado Version. Incluir este nuevo campo de Version permite realizar cambios continuamente del estado de los datos sin tener que estar cambiar el estado de los estados anterior guardados previamente.
- **GraphiteMergeTree**: tabla diseñada para almacenar datos para Graphite.

Indicar que los procesos de compactación de cualquiera de los tipos de tablas indicados anteriormente, ClickHouse, los realiza en background y en cualquier momento sin posibilidad de planificación.

Para entrar más en detalle sobre este tipo de tablas os recomiendo leer el apartado relativo a ellas en [MergeTree Family](https://clickhouse.yandex/docs/en/operations/table_engines/mergetree/).

Por otro lado tenemos otros tipos de tablas donde la característica especial es que no soportan indices. En este caso ClickHouse nos proporciona:

- **TinyLog**: son las tablas más simple destinada a almacenar pequeños volúmenes de datos no superiores al millón de filas. Se caracteriza por almacenar cada columna en diferentes ficheros comprimidos. Su uso son para situaciones donde se escribe una vez y luego se realizan multitud de consultas, habitualmente se utiliza para almacenar datos temporalmente que son procesados en pequeños batches. Hay que tener cuidado con este tipo de tablas ya que si se escribe y se lee al mismo tiempo las consultas retornarán un error.
- **Log**: similar a TinyLog pero añadiendo un pequeño fichero llamado "marks" que almacena información para ir saltando un determinado número de filas.
- **Memory**: son tablas cuyos valores se almacena en memoria y por tanto no se realiza ningún tipo de compresión de los datos. Los datos almacenados son volátiles, es decir, si se produce un reinicio se perderán. Este tipo de tablas se utiliza habitualmente para realizar pruebas.
- **Buffer**: son tablas que almacenan los datos en memoria y periódicamente la deposita en otra tabla.
- **External Data**: permite cargar recursos externos como si fueran tablas y usarlas para ser usadas en consultas.

Y por último existen otros tipos de tablas especiales:

- **Distributed**: realmente no es una tabla que contenga datos sino que permite distribuir las consultas sobre aquellos servidores donde se encuentran los datos y posteriormente unificarlos y retornar el resultado de la consulta.
- **Dictionary**: son como si fueran tablas maestras que se definen en la configuración de ClickHouse. Son cargados en memoria en el arranque del servidor estando disponibles para utilizarlas en las consultas.
- **Merge**: no confundir con la familia de tablas MergeTree, únicamente permite unir varias tablas leyéndolas de forma paralela. Estas tablas son sólo de lectura y no están permitidas las escrituras.
- **File**: son tablas creadas a partir de los valores de un fichero.
- **Null**: tabla como puede ser la tabla DUAL en Oracle. Las escrituras son ignoradas y las lecturas no retornan ninguna fila.
- **URL**: son tablas creadas a partir de los datos retornados de una URL.
- **View**: similar a las vistas de cualquier otra base de datos.
- **MaterializedView**: similar a las tablas materializadas de Oracle.
- **Kafka**: permite integrarnos con este sistema de mensajes consumiendo eventos que se produzcan en él.
- **MySQL**: permite almacenar en una base de datos remota de MySQL una query ejecutado sobre ClickHouse.


### 3.2 Interfaces de comunicación

ClickHouse ofrece dos formas de comunicarnos: mediante HTTP o por TCP utilizando su propio protocolo. Utilizando estas dos interfaces existen multitud de herramientas o drivers para acceder de forma más amigable. Yandex ofrece un cliente por línea de comandos similar al comando mysql y nos permite ejecutar cualquier tipo de consulta sobre ClickHouse. También ofrece drivers [JDBC](https://github.com/yandex/clickhouse-jdbc) y [ODBC](https://github.com/yandex/clickhouse-odbc) que permite usarse con cualquier librería de acceso a datos como puede ser Spring-JDBC, MyBatis, etc..

Indicar que estas dos implementaciones hacen uso del interfaz HTTP y el rendimiento puede ser un poco menor al nativo al tener mayor sobrecarga. En el caso de que quisiéramos utilizar un driver JDBC que utilice el interfaz nativo podríamos usar una librería de terceros [ClickHouse-Native-JDBC](https://github.com/housepower/ClickHouse-Native-JDBC) pero hay que indicar que la última versión tiene leaks de memoria y fallos en la conexión que en el momento del tutorial no aconsejamos utilizarlo.

Además de las librerías o herramientas que proporciona el propio Yandex existen implementaciones de terceros como: clientes para cualquier de los lenguajes más usado hoy día, aplicaciones visuales como puede ser Tabix, HouseOps, LightHouse, DBeaver, DataGrip, etc..


### 3.3 Ejemplo de uso

Una vez que hemos visto un poco las características básicas de ClickHouse vamos a realizar un ejemplo utilizando el driver JDBC de Yandex. Para el ejemplo vamos a utilizar la base de datos que proporciona Transtats sobre los vuelos de las aerolíneas desde 1987. Para nuestro ejemplo sólo cargaremos los años que van desde 2015 al 2017 que corresponde con más de 17 millones de registros y usaremos las imágenes Docker del servidor y cliente de ClickHouse que proporciona Yandex.

Lo primero es arrancar un servidor de ClickHouse, evidentemente antes debemos tener instalado Docker en nuestro sistema. Abrimos un terminal y tecleamos:

`docker run -d --name some-clickhouse-server -p 8123:8123 -p 9000:9000 -p 9009:9009  --ulimit nofile=262144:262144 yandex/clickhouse-server`A continuación, arrancamos el cliente de ClickHouse y accedemos al cliente abriendo una shell para instalar los comandos wget y unzip necesarios para descargar y cargar los datos en ClickHouse:

```
 > docker run -it --name some-clickhouse-client --rm --link some-clickhouse-server:clickhouse-server yandex/clickhouse-client --host clickhouse-server
 > docker exec -it some-clickhouse-client bash
 root@adaf832bf64d:/> apt-get  update && apt-get install wget 
```

Ya tenemos preparado nuestro entorno para cargar los datos. Lo primero nos descargamos la información de cada mes desde el 2015 al 2017:

```
root@adaf832bf64d:/>  for s in `seq 2015 2017`
do
for m in `seq 1 12`
do
wget https://transtats.bts.gov/PREZIP/On_Time_Reporting_Carrier_On_Time_Performance_1987_present_${s}_${m}.zip

done
done
```

Ahora creamos la tabla en ClickHouse donde vamos a almacenar los datos. Arrancamos un cliente de ClickHouse:

```
root@adaf832bf64d:/> clickhouse-client --host clickhouse-server
ClickHouse client version 18.14.18.
Connecting to clickhouse-server:9000.
Connected to ClickHouse server version 18.14.18 revision 54409.

```

Y ejecutamos la siguiente sentencia:

```sql 
CREATE TABLE ontime (
  Year UInt16,
  Quarter UInt8,
  Month UInt8,
  DayofMonth UInt8,
  DayOfWeek UInt8,
  FlightDate Date,
  UniqueCarrier FixedString(7),
  AirlineID Int32,
  Carrier FixedString(2),
  TailNum String,
  FlightNum String,
  OriginAirportID Int32,
  OriginAirportSeqID Int32,
  OriginCityMarketID Int32,
  Origin FixedString(5),
  OriginCityName String,
  OriginState FixedString(2),
  OriginStateFips String,
  OriginStateName String,
  OriginWac Int32,
  DestAirportID Int32,
  DestAirportSeqID Int32,
  DestCityMarketID Int32,
  Dest FixedString(5),
  DestCityName String,
  DestState FixedString(2),
  DestStateFips String,
  DestStateName String,
  DestWac Int32,
  CRSDepTime Int32,
  DepTime Int32,
  DepDelay Int32,
  DepDelayMinutes Int32,
  DepDel15 Int32,
  DepartureDelayGroups String,
  DepTimeBlk String,
  TaxiOut Int32,
  WheelsOff Int32,
  WheelsOn Int32,
  TaxiIn Int32,
  CRSArrTime Int32,
  ArrTime Int32,
  ArrDelay Int32,
  ArrDelayMinutes Int32,
  ArrDel15 Int32,
  ArrivalDelayGroups Int32,
  ArrTimeBlk String,
  Cancelled UInt8,
  CancellationCode FixedString(1),
  Diverted UInt8,
  CRSElapsedTime Int32,
  ActualElapsedTime Int32,
  AirTime Int32,
  Flights Int32,
  Distance Int32,
  DistanceGroup UInt8,
  CarrierDelay Int32,
  WeatherDelay Int32,
  NASDelay Int32,
  SecurityDelay Int32,
  LateAircraftDelay Int32,
  FirstDepTime String,
  TotalAddGTime String,
  LongestAddGTime String,
  DivAirportLandings String,
  DivReachedDest String,
  DivActualElapsedTime String,
  DivArrDelay String,
  DivDistance String,
  Div1Airport String,
  Div1AirportID Int32,
  Div1AirportSeqID Int32,
  Div1WheelsOn String,
  Div1TotalGTime String,
  Div1LongestGTime String,
  Div1WheelsOff String,
  Div1TailNum String,
  Div2Airport String,
  Div2AirportID Int32,
  Div2AirportSeqID Int32,
  Div2WheelsOn String,
  Div2TotalGTime String,
  Div2LongestGTime String,
  Div2WheelsOff String,
  Div2TailNum String,
  Div3Airport String,
  Div3AirportID Int32,
  Div3AirportSeqID Int32,
  Div3WheelsOn String,
  Div3TotalGTime String,
  Div3LongestGTime String,
  Div3WheelsOff String,
  Div3TailNum String,
  Div4Airport String,
  Div4AirportID Int32,
  Div4AirportSeqID Int32,
  Div4WheelsOn String,
  Div4TotalGTime String,
  Div4LongestGTime String,
  Div4WheelsOff String,
  Div4TailNum String,
  Div5Airport String,
  Div5AirportID Int32,
  Div5AirportSeqID Int32,
  Div5WheelsOn String,
  Div5TotalGTime String,
  Div5LongestGTime String,
  Div5WheelsOff String,
  Div5TailNum String
) ENGINE = MergeTree(FlightDate, (Year, FlightDate), 8192)
```


Salimos del cliente ejecutando el comando "exit" y a continuación, cargamos los datos utilizando el cliente de ClickHouse:

```
root@adaf832bf64d:/> for i in *.zip; do echo $i; unzip -cq $i '*.csv' | sed 's/.00//g' | clickhouse-client --host=clickhouse-server --query="INSERT INTO ontime FORMAT CSVWithNames"; done

```

Esto nos llevará un poco de tiempo. Una vez que el proceso de carga haya finalizado podemos realizar una prueba para ver si se han cargado los datos.

```
root@adaf832bf64d:/> clickhouse-client --host clickhouse-server
ClickHouse client version 18.14.18.
Connecting to clickhouse-server:9000.
Connected to ClickHouse server version 18.14.18 revision 54409.

4d42b6740871 :)  select count() from ontime

SELECT count()
FROM ontime 

┌──count()─┐
│ 17597523 │
└──────────┘

1 rows in set. Elapsed: 0.031 sec. Processed 17.60 million rows, 17.60 MB (569.78 million rows/s., 569.78 MB/s.) 

4d42b6740871 :) 

```

Ahora vamos a crear una pequeña aplicación REST con Spring-Boot y usando el driver JDBC de Yandex con Spring-JDBC. Creamos un proyecto Maven con el siguiente pom.xml:

```xml 
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.autenita.training</groupId>
    <artifactId>spring-boot-training</artifactId>
    <version>0.0.1-SNAPSHOT</version>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.1.RELEASE</version>
    </parent>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>ru.yandex.clickhouse</groupId>
            <artifactId>clickhouse-jdbc</artifactId>
            <version>0.1.48</version>
        </dependency>
    </dependencies>
</project>
```


En el directorio de resources creamos el fichero de configuración de Spring-Boot (application.yaml) bajo en el directorio config:

```yaml 
debug: true
logging:
   path: /tmp
   file:
      max-size: 10MB
      max-history: 2
   group:
      tomcat: org.apache.catalina, org.apache.coyote, org.apache.tomcat            
   level:
      root: WARN
      tomcat: ERROR
      web: DEBUG
      org.hibernate: ERROR
      org.springframework.data: DEBUG
spring:
   datasource:
      driver-class-name: ru.yandex.clickhouse.ClickHouseDriver
      username: default
      password: 
      url: jdbc:clickhouse://127.0.0.1:8123
      connectionTimeout: 67000
      hikari:
         connectionTimeout: 67000
         idleTimeout: 600000
         maxLifetime: 1800000                  
```

Como podemos ver hemos configurado la conexión con ClickHouse que utilizará Spring-JDBC utilizando la librería de Hikari.

Ahora creamos la clase de arranque de Spring-Boot:

```java 
package com.autentia.training.springboot;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ApplicationMain {

    public static void main(String[] args) throws Exception {
        new SpringApplicationBuilder(ApplicationMain.class)
            .run(args);
    }
}

```


En el ejemplo vamos a crear un endpoint que nos retorne el número de vuelos de cada aerolínea filtrando por año. Para ello nos creamos el controlador que reciba la petición:

```java 
package com.autentia.training.springboot.statistics.airlines.rest;

import java.util.List;

import javax.validation.constraints.Size;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.autentia.training.springboot.statistics.airlines.model.AirlineFlightsNumber;
import com.autentia.training.springboot.statistics.airlines.service.StatisticsAirlinesService;

@RestController
@RequestMapping("/statistics/airlines")
public class StatisticsAirlinesController {

    private StatisticsAirlinesService statisticsService;

    public StatisticsAirlinesController(StatisticsAirlinesService statisticsService) {
        this.statisticsService = statisticsService;

    }

    @GetMapping("/flights")
    public List<AirlineFlightsNumber> getFlightsByYear(@Size(min=4, max= 4) @RequestParam("year") int year) {
        return statisticsService.getFlightsByYear(year);
    }

}

```


Ahora creamos el servicio:

```java 
package com.autentia.training.springboot.statistics.airlines.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.autentia.training.springboot.statistics.airlines.model.AirlineFlightsNumber;
import com.autentia.training.springboot.statistics.airlines.repository.StatisticsAirlinesRepository;

@Service
public class StatisticsAirlinesService {

    private StatisticsAirlinesRepository statisticsRepository;

    public StatisticsAirlinesService (StatisticsAirlinesRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;

    }

    public List<AirlineFlightsNumber> getFlightsByYear(int year) {
        return statisticsRepository.getFlightsByYear(year);
    }

}

```

Ahora el repositorio:

```java 
package com.autentia.training.springboot.statistics.airlines.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.autentia.training.springboot.statistics.airlines.model.AirlineFlightsNumber;

@Repository
public class StatisticsAirlinesRepository {

    private JdbcTemplate jdbcTemplate;

    public StatisticsAirlinesRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

    }

    public List<AirlineFlightsNumber> getFlightsByYear(int year) {
        return jdbcTemplate.query(
                " select Carrier, sum(AirlineID) as flights from ontime where Year = ? group by Carrier order by flights desc",
                new Object[] { year }, (rs, rowNum) -> {
                    return new AirlineFlightsNumber(rs.getString("Carrier"), rs.getLong("flights"));
                });
    }

}

```

Y por último el modelo:

```java 
package com.autentia.training.springboot.statistics.airlines.model;

public class AirlineFlightsNumber {

    private final String airline;
    private final long flights;

    public AirlineFlightsNumber(String airline, long flights) {
        this.airline = airline;
        this.flights = flights;
    }

    public String getAirline() {
        return airline;
    }

    public long getFlights() {
        return flights;
    }

}

```

Ahora arrancamos la aplicación y con un client ejecutamos por ejemplo que nos retorne los vuelos por aerolínea realizados en 2016:

```json 
GET http://localhost:8080/statistics/airlines/flights?year=2016

[
    {
        "airline": "WN",
        "flights": 25200117492
    },
    {
        "airline": "DL",
        "flights": 18261143340
    },
    {
        "airline": "AA",
        "flights": 18111573475
    },
    {
        "airline": "OO",
        "flights": 12302863632
    },
    {
        "airline": "UA",
        "flights": 10888803459
    },
    {
        "airline": "EV",
        "flights": 9999502340
    },
    {
        "airline": "B6",
        "flights": 5764991457
    },
    {
        "airline": "AS",
        "flights": 3533190400
    },
    {
        "airline": "NK",
        "flights": 2821470784
    },
    {
        "airline": "F9",
        "flights": 1943892756
    },
    {
        "airline": "HA",
        "flights": 1511975410
    },
    {
        "airline": "VX",
        "flights": 1463360691
    }
]
```

Se podéis ver los tiempos de respuesta de la petición esta por debajo de 50 ms recorriendo los más de 17 millones de registros.

Se puede descargar el código completo desde:
```
git clone https://github.com/angelusGJ/clickhouse_base_example
```

## 4. Conclusión

Como podéis visto trabajar con ClickHouse para realizar analítica del dato es bastante sencillo ya que se trabajaría como cualquier otra base de datos. Lo realmente importante a la hora de trabajar con este tipo de base de datos es estructurar correctamente la información en base a las consultas que se vayan a realizar y elegir correctamente el tipo de tabla correcto.

Lo único que hecho en falta es más documentación ya que la única documentación que existe es la documentación oficial. Aunque también hay que decir que es bastante buena y es suficiente para exprimir al máximo.  


## 5. Referencias

- <https://clickhouse.yandex/docs/en/>
- <https://hub.docker.com/r/yandex/clickhouse-server/>
- [https://transtats.bts.gov/Tables.asp?DB_ID=120&DB_Name=Airline%20On-Time%20Performance%20Data&DB_Short_Name=On-Time](https://transtats.bts.gov/Tables.asp?DB_ID=120&DB_Name=Airline%20On-Time%20Performance%20Data&DB_Short_Name=On-Time)