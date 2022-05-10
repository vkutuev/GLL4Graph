![example workflow](https://github.com/JetBrains-Research/GLL4Graph/actions/workflows/main.yml/badge.svg)

# GLL4Graph

This is the implementation of the GLL-based context-free path querying (CFPQ) algorithm. It is based on a high-performance GLL parsing algorithm implemented in [Iguana](https://iguana-parser.github.io/) project. Then, it was modificated to support graph-structured input data. Proposed solution solves both reachability and all paths problems for all pairs and multiple sources cases.

## Performance

The proposed solution has been evaluated on several real-world graphs for both the all pairs and the multiple sources scenarios. The evaluation shows that the proposed solution is more than 45 times faster than the previous solution for Neo4j and is comparable, in some scenarios and cases, with the linear algebra based solution.

**Machine configuration**: PC with Ubuntu 20.04, Intel Core i7-6700 3.40GHz CPU, DDR4 64Gb RAM.

**Enviroment configuration**: 
* Java HotSpot(TM) 64-Bit server virtual machine (build 15.0.2+7-27, mixed mode, sharing).
* JVM heap configuration: 55Gb both xms and xmx.
* Neo4j 4.0.3 is used. Almost all configurations are default except two:
     * total off-heap transaction memory (tx_state_max_off_heap_memory parameter) is set to 24Gb
     * pagecache_warmup_enabled is set to true.


### Graphs

The graph data is selected from [CFPQ_Data dataset](https://github.com/JetBrains-Research/CFPQ_Data). There are two types of graph:
* Graphs related to RDF analysis problems
* Graphs related to static code analysis problems

A detailed description of the graphs is listed bellow.

**RDF analysis** 

| Graph name   |  \|*V*\|   |  \|*E*\|   | #subClassOf |   #type   | #broaderTransitive |
|:-------------|:----------:|:----------:|:-----------:|:---------:|:------------------:|
| Core         |   1 323    |  2 752     |       178   |     0     |         0          |
| Pathways |   6 238   |  12 363   |   3 117   |     3 118     |         0          |
| Go hierarchy |   45 007   |  490 109   |   490 109   |     0     |         0          |
| Enzyme       |   48 815   |   86 543   |    8 163    |  14 989   |       8 156        | 
| Eclass_514en |  239 111   |  360 248   |   90 962    |  72 517   |         0          | 
| Geospecies   |  450 609   | 2 201 532  |      0      |  89 065   |       20 867       | 
| Go           |  582 929   | 1 437 437  |   94 514    |  226 481  |         0          | 
| Taxonomy     | 5 728 398  | 14 922 125 |  2 112 637  | 2 508 635 |         0          |

**Static code analysis**

| Graph name   |  \|*V*\|   |  \|*E*\|   |    #a     |    #d     |
|:-------------|:----------:|:----------:|:---------:|:---------:|
| Apache       |  1 721 418 |  1 510 411  |  362 799 |  1 147 612 | 
| Block          |  3 423 234 |  2 951 393  |  669 238 |  2 282 155 | 
| Fs             |  4 177 416 |  3 609 373 |  824 430 |  2 784 943 | 
|       Ipc            |  3 401 022 |  2 931 498 |  664 151 |  2 267 347 | 
|     Lib            |  3 401 355 |  2 931 880 |  664 311 |  2 267 569 | 
|   Mm             |  2 538 243 |  2 191 079 |  498 918 |  1 692 161 | 
|  Net            |  4 039 470 |  3 500 141 |  807 162 |  2 692 979 | 
| Postgre       |  5 203 419 |  4 678 543 |  1 209 597 |  3 468 946 | 
| Security       |  3 479 982 |  3 003 326 |  683 339 |  2 319 987 | 
| Sound          |  3 528 861 |  3 049 732 |  697 159 |  2 352 573 | 
| Init           |  2 446 224 |  2 112 809 | 481 994 |  1 630 815 | 
| Arch           |  3 448 422 |  2 970 242 |  6 712 95 |  2 298 947 | 
| Crypto         |  3 464 970 |  2 988 387 |  678 408 |  2 309 979 | 
| Drivers        |  4 273 803 |  3 707 769 |  858 568 |  2 849 201 | 
| Kernel         |  11 254 434 |  9 484 213  |  1 981 258 |  7 502 955 | 

### Grammars

All queries used in evaluation are variants of same-generation query. The inverse of an ```x``` relation and the respective edge is denoted as ```x_r```.

<br/>

Grammars used for **RDF** graphs:

**G<sub>1</sub>**
```
S -> subClassOf_r S subClassOf | subClassOf_r subClassOf 
     | type_r S type | type_r type
```

**G<sub>2</sub>**
```
S -> subClassOf_r S subClassOf | subClassOf
```

  **Geo**
```
S -> broaderTransitive S broaderTransitive_r
     | broaderTransitive broaderTransitive_r 
```

<br/>

Grammar used for **static code analysis** graphs:
  
**PointsTo**
  ```
  M -> d_r V d
  V -> (M? a_r)* M? (a M?)* 
  ```

### Results
  
The results of the **all pairs reachability** queries evaluation on graphs related to **RDF analysis**:

<table>
  <thead>
    <tr>
      <th rowspan="2" align="left">Graph name</th>
      <th colspan="2" align="center">G<sub>1</sub></th>
      <th colspan="2" align="center">G<sub>2</sub></th>
      <th colspan="2" align="center">Geo</th>
    </tr>
    <tr>
      <td align="center">time (sec)</td>
      <td>#answer</td>
      <td align="center">time (sec)</td>
      <td align="center">#answer</td>
      <td align="center">time (sec)</td>
      <td align="center">#answer</td>
    </tr>
  </thead>
  <tbody>
      <tr>
      <td align="left">Core</td>
      <td align="center">0,02</td>
      <td>204</td>
      <td align="center">0,01</td>
      <td align="center">214</td>
      <td align="center">–</td>
      <td align="center">–</td>
    </tr><tr>
      <td align="left">Pathways</td>
      <td align="center">0,07</td>
      <td>884</td>
      <td align="center">0,04</td>
      <td align="center">3117</td>
      <td align="center">–</td>
      <td align="center">–</td>
    </tr>
    <tr>
      <td align="left">Go hierarchy</td>
      <td align="center">3,68</td>
      <td>588 976</td>
      <td align="center">5,42</td>
      <td align="center">738 937</td>
      <td align="center">–</td>
      <td align="center">–</td>
    </tr>
    <tr>
      <td align="left">Enzyme</td>
      <td align="center">0,22</td>
      <td>396</td>
      <td align="center">0,17</td>
      <td align="center">8163</td>
      <td align="center">5,7</td>
      <td align="center">14 267 542</td>
    </tr>
    <tr>
      <td align="left">Eclass</td>
      <td align="center">1,5</td>
      <td>90 994</td>
      <td align="center">0,97</td>
      <td align="center">96 163</td>
      <td align="center">–</td>
      <td align="center">–</td>
    </tr>
    <tr>
      <td align="left">Geospecies</td>
      <td align="center">2,89</td>
      <td>85</td>
      <td align="center">2,65</td>
      <td align="center">0</td>
      <td align="center">145,8</td>
      <td align="center">226 669 749</td>
    </tr>
    <tr>
      <td align="left">Go</td>
      <td align="center">5,56</td>
      <td>640 316</td>
      <td align="center">4,24</td>
      <td align="center">659 501</td>
      <td align="center">–</td>
      <td align="center">–</td>
    </tr>
    <tr>
      <td align="left">Taxonomy</td>
      <td align="center">45,47</td>
      <td>151 706</td>
      <td align="center">36,06</td>
      <td align="center">2 112 637</td>
      <td align="center">–</td>
      <td align="center">–</td>
    </tr>
  </tbody>
</table>

The results of the **all pairs reachability** queries evaluation on graphs related to **static code analysis**:

<table>
  <thead>
    <tr>
      <th rowspan="2" align="left">Graph name</th>
      <th colspan="2" align="center">PointsTo</th>
    </tr>
    <tr>
      <td align="center">time (sec)</td>
      <td>#answer</td>
    </tr>
  </thead>
  <tbody>
      <tr>
      <td align="left">Apache</td>
      <td align="center">-</td>
      <td align="center">-</td>
    </tr>
      <tr>
      <td align="left">Block</td>
      <td align="center">113,01</td>
      <td align="center">5 351 409</td>
    </tr>
      <tr>
      <td align="left">Fs</td>
      <td align="center">167,73</td>
      <td align="center">9 646 475</td>
    </tr>
      <tr>
      <td align="left">Ipc</td>
      <td align="center">109,43</td>
      <td align="center">5 249 389</td>
    </tr>
      <tr>
      <td align="left">Lib</td>
      <td align="center">111,09</td>
      <td align="center">5 276 303</td>
    </tr>
      <tr>
      <td align="left">Mm</td>
      <td align="center">77,92</td>
      <td align="center">3 990 305</td>
    </tr>
      <tr>
      <td align="left">Net</td>
      <td align="center">160,64</td>
      <td align="center">8 833 403</td>
    </tr>
      <tr>
      <td align="left">Postgre</td>
      <td align="center">-</td>
      <td align="center">-</td>
    </tr>
      <tr>
      <td align="left">Security</td>
      <td align="center">115,75</td>
      <td align="center">5 593 387</td>
    </tr>
      <tr>
      <td align="left">Sound</td>
      <td align="center">120,14</td>
      <td align="center">6 085 269</td>
    </tr>
    <tr>
      <td align="left">Init</td>
      <td align="center">87,25</td>
      <td align="center">3 783 769</td>
    </tr>
    <tr>
      <td align="left">Arch</td>
      <td align="center">130,77</td>
      <td align="center">5 339 563</td>
    </tr>
    <tr>
      <td align="left">Crypto</td>
      <td align="center">128,8</td>
      <td align="center">5 428 237</td>
    </tr>
    <tr>
      <td align="left">Drivers</td>
      <td align="center">371,18</td>
      <td align="center">18 825 025</td>
    </tr>
    <tr>
      <td align="left">Kernel</td>
      <td align="center">614,047</td>
      <td align="center">16 747 731</td>
    </tr>
  </tbody>
</table>

<br/>

The evaluation results for **single source** CFPQ for graphs related to **RDF analysis** and **G<sub>1**, **G<sub>2**, **Geo** grammars respectively in **reachability** and **all paths** scenarious:

![time](https://github.com/JetBrains-Research/GLL4Graph/blob/master/docs/pictures/ss-g1.png)
![time](https://github.com/JetBrains-Research/GLL4Graph/blob/master/docs/pictures/ss-g2.png)
![time](https://github.com/JetBrains-Research/GLL4Graph/blob/master/docs/pictures/ss-geo.png)

The evaluation results for **single source** CFPQ for graphs related to **static code analysis** and **pointsTo** grammar in **reachability** and **all paths** scenarious:

![time](https://github.com/JetBrains-Research/GLL4Graph/blob/master/docs/pictures/ss-stat.png)
    
## Download and build

The project is build with Maven.

```
git clone https://github.com/JetBrains-Research/GLL4Graph
cd GLL4Graph
mvn compile
```
## Usage

To replicate experiments:

* make the directory to save results
```
mkdir results
```
* run the following command with arguments
```
mvn exec:java -Dexec.mainClass="benchmark.Neo4jBenchmark" -Dexec.args="arg1 arg2 arg3 arg4 arg5 agr6 agr7 arg8"
```
Argument | Description
:--- | :---
arg1 | Relationship types of the edges in graph to use. Currently this argument can take one of three possible values: <br> - **st** is used for G<sub>1</sub> and G<sub>2</sub> grammars  <br> - **bt** is used for Geo grammar <br> - **ad** is used for PointsTo grammar
arg2 | The number of vertices in graph
arg3 | The number of warm up iterations
arg4 | The total number of iterations
arg5 | Path to dataset
arg6 | Path to grammar
arg7 | The name of a file with result
arg8 | Grammar name (g1/g2/geo/pointsTo)

### Example

Here is an example which can be run right after project is downloaded and results directory is created without any additional preparation.

To run experiments on **Core** graph on **G<sub>1</sub>** grammar use the following command:

```
mvn exec:java -Dexec.mainClass="benchmark.Neo4jBenchmark" -Dexec.args="st 1323 2 5 <ABS_PATH_TO_PROJECT>/data/core/ test/resources/grammars/graph/g1/grammar.json core g1"
```

### Data
To get more graph data examples use Python script:

```
graph_loader.py --graph {graph_name} --relationships {comma_separated_relationships_list}
```

For example:
```
graph_loader.py --graph core --relationships subClassOf,type
```

## License

This project is licensed under OpenBSD License. License text can be found in the 
[license file](https://github.com/JetBrains-Research/GLL4Graph/blob/GLL-for-graph/LICENSE.md).
