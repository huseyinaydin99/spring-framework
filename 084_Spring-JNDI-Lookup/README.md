### JNDI Lookup – Uygulama Sunucusu, IoC ve Benim Gözümden Hikâyesi ⚙️🔌

JNDI ve JNDI lookup ile ilk tanıştığımda, aslında çözmeye çalıştığım problem çok basitti: “Veritabanı bağlantı detaylarını kodumun içinden söküp atmak ve ortamdan bağımsız, daha temiz, daha yönetilebilir bir mimari kurmak istiyorum.”; zaman içinde fark ettim ki JNDI lookup sadece teknik bir ayrıntı değil, uygulama sunucusu ile Spring IoC konteyneri arasında kurduğum köprünün tam ortasında duran stratejik bir parça haline gelmiş. 💡

---

#### JNDI ve JNDI Lookup Nedir, Ne Değildir? 🧩

JNDI’yi kendi bakış açımdan, sistemdeki kaynakları isim üzerinden bulup kullanmamı sağlayan, bir nevi “ortak adres defteri” gibi görüyorum; veritabanı, mesaj kuyruğu, mail sunucusu ya da dizin servisi gibi dış kaynakları, sabit bir isimle işaretleyip, kod tarafında bu isme bakarak erişmemi sağlayan bir isimlendirme ve dizin mekanizmasıdır. 📒

JNDI lookup dediğim şey ise, tam olarak bu isim–adres dünyasında yaptığım “çalışma anı araması”dır; uygulama sunucusunda önceden tanımlanmış `jdbc/huseyinaydin` gibi bir kaynağa, Spring üzerinden `jee:jndi-lookup` ile başvurup, oradaki gerçek `DataSource` nesnesine erişmemi ve bu nesneyi kendi IoC konteynerimde bean gibi kullanmamı sağlayan adımdır. 🔍

- JNDI, nesnelerin ve kaynakların “nerede olduklarını” düşünmek zorunda kalmadan, sadece “hangi isimle çağıracağımı” bilerek onlara erişmemi sağlayan, ortamdan bağımsız bir soyutlama katmanıdır; bu sayede kod tarafında veritabanı URL’i, kullanıcı adı, şifre ya da pool ayarları ile boğuşmak yerine, sadece mantıksal bir isim üzerinden hareket ederim.
- JNDI lookup ise, bu soyutlamayı çalışma anına taşıyarak, uygulama ayağa kalktığında ya da ihtiyaç duyduğum anda, sunucunun içinde zaten oluşturup yönettiği `DataSource` nesnesine bir referans almamı sağlayan ve bu referansı Spring’in IoC konteynerine bean olarak kaydettiren operasyonel adımdır.

---

#### JNDI Lookup Ne Amaçla Vardır, Neden Kullanılır? 🎯

Benim için JNDI lookup’ın var olma sebebi, veritabanı bağlantıları gibi kritik altyapı bileşenlerini, kodun içindeki sabit tanımlardan kurtarıp, uygulama sunucusuna devretmek ve aynı kaynağı birden fazla uygulamanın güvenli bir şekilde paylaşabilmesini sağlamaktır; kısacası bağlantı detaylarının “uygulamadan soyutlanması ve merkezileştirilmesi” ana hedeftir. 🏛️

- JNDI lookup sayesinde, veritabanı bağlantı bilgilerimi (URL, kullanıcı, şifre, pool parametreleri vb.) war dosyamın içine gömmek zorunda kalmam, bunun yerine bu bilgiler Tomcat, WildFly gibi sunucuların konfigürasyon dosyalarında tutulur ve ben sadece bu sunucuların sağladığı JNDI ismine güvenerek bağlantıya ulaşırım.
- Bu yaklaşım, güvenlik açısından da önemli bir avantaj yaratır; çünkü prod ortamındaki hassas kullanıcı bilgilerini ve şifreleri kod deposundan, CI/CD pipeline’larından ve geliştirici makinelerinden olabildiğince uzak tutup, bunları sadece uygulama sunucusunun yönettiği kapalı bir yapı içinde konumlandırmış olurum.

---

#### JNDI Kullanılmazsa Ne Olur, Ne Kaybederim? ⚠️

Eğer JNDI ve JNDI lookup kullanmazsam, veritabanı bağlantısını çoğu zaman doğrudan uygulamanın kendi konfigürasyonunda tanımlamak zorunda kalırım ve bu da ortam geçişlerinde (dev → test → prod) her seferinde war içeriğini veya config dosyalarını değiştirmek anlamına gelir; bu da hem tekrar, hem risk, hem de operasyonel yük demektir. 🧯

- JNDI olmadan, Spring içinde doğrudan `DriverManagerDataSource` gibi bir bean tanımlamam gerekir ve bu bean’in içinde veritabanı URL’ini, kullanıcı adını, şifreyi ve diğer ayarları doğrudan yazdığım için, farklı ortamlara göre profiller hazırlama, config dosyalarını dışarı alma gibi ek yapılandırma yükleri taşımak zorunda kalırım.
- JNDI kullanmadığım bir senaryoda, aynı veritabanına bağlanan farklı uygulamalar varsa, her bir uygulamanın kendi içinde ayrı ayrı connection pool oluşturması, sistem kaynağını gereksiz yere tüketir ve yönetmesi zor, parçalı bir altyapı ortaya çıkar.

---

#### JNDI Lookup’ın Ana Amacı ve Benim Mimarimdeki Yeri 🧱

Ben JNDI lookup’ı, uygulama sunucusu ile Spring IoC konteyneri arasında duran kritik bir adaptör gibi konumlandırıyorum; bir tarafta sunucunun oluşturup yönettiği `DataSource` nesnesi, diğer tarafta ise bu nesneyi DI ile kullanan repository ve servislerim var. 🤝

- JNDI lookup ile Spring konteynerine `dataSource` bean’ini eklediğimde, aslında kendi kodumda hiç “nasıl bir pool, kaç bağlantı, hangi driver” gibi detaya dokunmuyorum; tüm bu ayrıntılar sunucuya ait konfigürasyon tarafından belirleniyor ve Spring sadece o hazır nesneye referans alarak üst katmanları besliyor.
- Böylece mimarimdeki sorumluluk dağılımı netleşiyor: bağlantı yönetimi, pooling, erişim sayısı sınırları gibi altyapı konuları uygulama sunucusunda; entity, repository, servis, controller gibi iş mantığı ve akışlar ise Spring tarafında kalıyor ve ben bu ayrımı bilinçli olarak koruyabiliyorum.

---

#### Hangi Durumlarda JNDI Tercih Etmeliyim, Hangi Durumlarda Etmemeliyim? 🧭

JNDI ve JNDI lookup kullanıp kullanmayacağıma karar verirken, uygulamanın nasıl deploy edildiği, kaç uygulamanın aynı veritabanını paylaştığı ve ortam geçişlerini nasıl yönettiğim gibi soruları kendime soruyorum; her senaryoda JNDI kullanmak zorunlu değil, ama bazı durumlarda büyük avantaj getiriyor. ⚖️

- Eğer uygulamam klasik bir application server üzerinde (Tomcat, WildFly, WebLogic vb.) war/ear olarak çalışıyorsa, aynı veritabanına bağlanan birden fazla uygulama varsa ve bağlantı yönetimini merkezi yapmak istiyorsam, JNDI kullanmak benim için neredeyse doğal bir tercih haline geliyor.
- Buna karşılık, uygulamam bir Spring Boot jar’ı olarak container içinde (Docker/Kubernetes) tek başına çalışıyorsa ve veritabanı bağlantılarını genellikle environment değişkenleri ya da dış config server üzerinden yönetiyorsam, o noktada Spring’in kendi `DataSource` otomatik konfigürasyonlarını ve profil mekanizmasını kullanmak, JNDI’ye göre daha yalın ve pratik olabiliyor.

---

#### JNDI Lookup, Yazılıma ve Yazılımcıya Ne Katar? 👨‍💻🚀

Benim perspektifimden bakınca, JNDI lookup doğrudan kod kalitemi ve mimari esnekliğimi etkileyen bir yapı taşı; sadece veritabanı bağlantısı değil, aynı zamanda “sorumlulukları doğru yere koyma” refleksimi de güçlendiriyor. 🧠

- Yazılım tarafında, uygulama kodumu bağlantı detaylarından arındırdığım için, repository ve servis katmanlarım sadece `DataSource` arayüzüne güveniyor ve bu da test edilebilirlik, mock’lanabilirlik ve bağımlılıkların sadeleşmesi anlamına geliyor; bu sayede aynı kodu, farklı veri kaynaklarıyla daha rahat çalıştırıp deneyebiliyorum.
- Yazılımcı olarak benim açımdan ise, veritabanı URL’i değişti, kullanıcı bilgisi güncellendi, pool ayarları revize edildi gibi durumlarda kodu yeniden deploy etmeden, sadece sunucu tarafındaki JNDI resource konfigürasyonunu değiştirerek hızlıca adapte olabilmek, operasyonel anlamda bana ciddi konfor sağlıyor.

---

#### JNDI Lookup Özellikleri, Avantajları ve Dezavantajları 📊

Aşağıdaki tabloyu, JNDI lookup ile doğrudan Spring içinde `DataSource` tanımlama yaklaşımını kafamda netleştirmek için kullanıyorum; bu ikisini yan yana koyduğumda, hangi durumda hangisini seçmem gerektiğini daha rahat tartabiliyorum. ⚖️

| Konu / Kıyas Noktası 🧩 | JNDI Lookup (Sunucu Tabanlı) 🔌 | Doğrudan Spring DataSource (Uygulama Tabanlı) 🧪 |
|------------------------|---------------------------------|--------------------------------------------------|
| Tanım Yeri            | Veritabanı kaynağı uygulamadan bağımsız olarak uygulama sunucusunun konfigürasyonunda tanımlanır ve uygulamalar bu kaynağa JNDI ismi üzerinden erişir; böylece aynı kaynağı birden fazla uygulama paylaşabilir. | Veritabanı bağlantı bilgileri doğrudan uygulamanın kendi konfigürasyonunda veya Spring bean tanımında tutulur ve her uygulama genelde kendi pool’una ve parametrelerine sahip olur. |
| Esneklik              | Ortam geçişlerinde yalnızca sunucu tarafındaki JNDI resource tanımını güncellemek yeterlidir; kodu veya war dosyasını değiştirmeden URL, kullanıcı, şifre gibi değerler değiştirilebilir. | Ortam değiştikçe (dev/test/prod) uygulama konfigürasyonlarını ayrı ayrı yönetmek gerekir; Spring profilleri ve dış konfigürasyon ile bu yönetilebilir, ancak tüm kontrol uygulama tarafında kalır. |
| Güvenlik              | Hassas bilgiler (kullanıcı adı, parola, pool ayarları) uygulamanın dışında, sunucunun kapalı konfigürasyon dünyasında tutulur; CI/CD ve kod deposu bu bilgilere doğrudan maruz kalmaz. | Eğer iyi izole edilmezse, konfigürasyon dosyaları ve environment değişkenleri üzerinden hassas bilgelere daha fazla kişi erişebilir; bu da yanlış yapılandırmalarda güvenlik risklerini artırabilir. |
| Yönetilebilirlik      | Merkezi yönetim sayesinde aynı kaynağı kullanan tüm uygulamaların bağlantı ayarları tek noktadan yönetilir; DBA ve ops ekipleri için daha kontrollü bir yaklaşım sunar. | Her uygulama kendi DataSource’unu yönettiği için değişiklikleri her projede ayrı ayrı yapmak gerekir; bu da özellikle kurumsal ortamlarda yönetim yükünü artırabilir. |
| Bağımlılık            | Uygulama, belirli bir JNDI ismine bağımlıdır ve bu ismin sunucu tarafında doğru tanımlanmış olmasına güvenir; sunucuya bağımlılık bilincini taşımak gerekir. | Uygulama kendi DataSource’unu kontrol ettiği için sunucuya özel JNDI yapılandırmasına ihtiyaç duymaz; ancak bu sefer de config yönetimi yükü uygulamanın üzerinde kalır. |

Avantajları ve dezavantajları kısaca kendi cümlelerimle toparlamak gerekirse: JNDI lookup, özellikle klasik application server mimarisinde merkezi yönetim, güvenlik ve paylaşımlı kaynak kullanımı açısından çok güçlü bir araçtır; buna karşın, modern container tabanlı, self-contained Spring Boot uygulamalarında her zaman şart değildir ve bazen doğrudan Spring DataSource konfigürasyonu daha yalın bir çözüm olarak öne çıkar. ⚙️

---

#### JNDI ve Spring IoC / DI – İkisi Bir Arada Nasıl Çalışır? 🤝

Ben JNDI ile Spring IoC/DI ilişkisini, sunucuda duran ağır bir kaynak nesnesinin (örneğin connection pool) Spring konteynerine zarif bir şekilde “bean gibi gösterilmesi” olarak görüyorum; teknik olarak tek bir nesne var, ama bu nesnede hem sunucunun hem de Spring’in payı bulunuyor. 🧠

- Uygulama sunucusu, `context.xml` veya benzeri bir konfigürasyon dosyasında tanımladığım `Resource` bilgisine göre, uygun driver, URL, kullanıcı adı, şifre ve pool parametreleri ile tek bir `DataSource` oluşturur ve bunu kendi içinde yönetir; bağlantılar açılır, kapatılır, pool yönetimi sunucu tarafında kalır.
- Spring tarafında ise, aşağıdaki gibi bir `jee:jndi-lookup` tanımı yaparım ve bu tanım sayesinde sunucunun oluşturduğu `DataSource` nesnesine bir referans alarak, onu IoC konteynerinde `dataSource` id’li bir bean olarak kaydederim; ondan sonra `@Autowired` ile bu bean’i repository veya servis katmanına enjekte ettiğimde, sanki tamamen Spring tarafından oluşturulmuş bir nesneymiş gibi kullanırım.

---

#### Kod Üzerinden JNDI Lookup Örneği ve Kendi Yapılandırmam 🧪

Aşağıdaki XML yapılandırma, benim JNDI lookup yaklaşımımı ve Hibernate ile birlikte nasıl kullandığımı oldukça net özetliyor; burada hem JNDI tarafını, hem de `SessionFactory` ve transaction yönetimi tarafını aynı bütünün parçaları olarak görebiliyorum. 🔗

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xmlns:jdbc="http://www.springframework.org/schema/jdbc"
       xmlns:jee="http://www.springframework.org/schema/jee"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans
           https://www.springframework.org/schema/beans/spring-beans.xsd
           http://www.springframework.org/schema/context
           https://www.springframework.org/schema/context/spring-context.xsd
           http://www.springframework.org/schema/tx
           https://www.springframework.org/schema/tx/spring-tx.xsd
           http://www.springframework.org/schema/mvc
           https://www.springframework.org/schema/mvc/spring-mvc.xsd
           http://www.springframework.org/schema/jdbc
           https://www.springframework.org/schema/jdbc/spring-jdbc.xsd
           http://www.springframework.org/schema/jee
           https://www.springframework.org/schema/jee/spring-jee.xsd">

    <!-- Uygulama sunucusundaki JNDI DataSource'u Spring IoC içine bean olarak çekiyorum. -->
    <jee:jndi-lookup id="dataSource"
                     jndi-name="jdbc/huseyinaydin"
                     expected-type="javax.sql.DataSource" />

    <!-- Hibernate SessionFactory (Spring 5.x + Hibernate 5.x ile birlikte çalışacak şekilde yapılandırdım). -->
    <bean id="sessionFactory" class="org.springframework.orm.hibernate5.LocalSessionFactoryBean">
        <property name="dataSource" ref="dataSource" />
        <property name="annotatedClasses">
            <list>
                <value>tr.com.huseyinaydin.model.Payment</value>
            </list>
        </property>
        <property name="hibernateProperties">
            <props>
                <prop key="hibernate.dialect">org.hibernate.dialect.MySQL8Dialect</prop>
                <prop key="hibernate.show_sql">true</prop>
                <prop key="hibernate.format_sql">true</prop>
                <prop key="hibernate.hbm2ddl.auto">update</prop>
            </props>
        </property>
    </bean>

    <!-- Hibernate Transaction Manager ile @Transactional anotasyonlarını destekliyorum. -->
    <bean id="transactionManager"
          class="org.springframework.orm.hibernate5.HibernateTransactionManager">
        <property name="sessionFactory" ref="sessionFactory" />
    </bean>

    <!-- @Transactional anotasyonlarını otomatik olarak aktif ediyorum. -->
    <tx:annotation-driven transaction-manager="transactionManager" />

</beans>
```

Bu yapılandırmanın yanında, Maven tarafında da Spring ve Hibernate sürümlerini güncel tutarak, hem klasik XML tabanlı yapıdan kopmadan hem de olabildiğince modern bir stack ile ilerlemeye çalışıyorum. 📦

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>tr.com.huseyinaydin</groupId>
    <artifactId>payment-resource</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>war</packaging>

    <name>payment-resource</name>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <java.version>1.8</java.version>
        <spring.version>5.3.39</spring.version>
        <hibernate.version>5.6.15.Final</hibernate.version>
        <hibernate.validator.version>6.2.5.Final</hibernate.validator.version>
        <jackson.version>2.17.2</jackson.version>
        <mysql.version>8.0.33</mysql.version>
    </properties>

    <dependencies>
        <!-- Spring çekirdek ve web katmanı bağımlılıkları -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-core</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-jdbc</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-tx</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-orm</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-web</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-webmvc</artifactId>
            <version>${spring.version}</version>
        </dependency>

        <!-- Hibernate ORM -->
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>${hibernate.version}</version>
        </dependency>

        <!-- Bean Validation -->
        <dependency>
            <groupId>org.hibernate.validator</groupId>
            <artifactId>hibernate-validator</artifactId>
            <version>${hibernate.validator.version}</version>
        </dependency>
        <dependency>
            <groupId>javax.validation</groupId>
            <artifactId>validation-api</artifactId>
            <version>2.0.1.Final</version>
        </dependency>

        <!-- MySQL Connector/J -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>${mysql.version}</version>
        </dependency>

        <!-- Jackson -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-core</artifactId>
            <version>${jackson.version}</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>${jackson.version}</version>
        </dependency>

        <!-- Servlet API (sunucu sağlayacağı için provided) -->
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
            <version>4.0.1</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>payment-resource</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.14.1</version>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

Bu kodlar sayesinde, JNDI ile uygulama sunucusunun sunduğu `DataSource` nesnesini Spring IoC konteynerine taşıyıp, Hibernate ile birlikte sorunsuz bir şekilde kullanıyorum; hem veri erişim katmanımı sade tutuyor, hem de ortamlar arası geçişte sadece sunucu tarafındaki JNDI kaynak tanımlarını değiştirerek esnek bir yapı elde ediyorum. 🔄

---

#### Özet: JNDI Lookup ile Uygulama Sunucusuna Sırtımı Yaslamak 🪑

Sonuç olarak, JNDI lookup benim için veritabanı erişimini daha profesyonel bir noktaya taşıyan, aynı zamanda “sistem tasarımı” refleksimi güçlendiren bir araç; bağlantı detaylarını kodun dışına iterek, güçlü bir ayrık sorumluluk modeli kuruyor ve uygulama sunucusunun gücünü Spring uygulamamın içine zarif bir şekilde entegre etmiş oluyorum. 💪

Bu yaklaşımı kullanırken her zaman şunu aklımda tutuyorum: JNDI benim için bir zorunluluk değil, doğru ortamda kullanıldığında mimariyi sadeleştiren güçlü bir seçenek; klasik application server dünyasında neredeyse vazgeçilmez, modern container dünyasında ise duruma göre devreye alabileceğim, esnek bir yapı taşı olarak cebimde duruyor. 🌍


### Server.xml
Bu XML bloğu, uygulama sunucusu (Tomcat) üzerinde jdbc/huseyinaydin isimli, MySQL’e bağlanan ve connection pool özellikleri (maxActive, maxIdle, minIdle, maxWait vb.) tanımlanmış global bir JNDI DataSource kaynağı oluşturur. Yani veritabanı URL’i, kullanıcı adı, şifre ve havuz ayarları sunucu seviyesinde merkezi olarak burada tutulur ve uygulamalar bu kaynağı isim üzerinden kullanır.
```xml
<Resource
        name="jdbc/huseyinaydin"
        global="jdbc/huseyinaydin"
        auth="Container"
        type="javax.sql.DataSource"
        driverClassName="com.mysql.cj.jdbc.Driver"
        url="jdbc:mysql://localhost:3306/huseyin_aydin_db?useSSL=false&amp;serverTimezone=UTC"
        username="root"
        password="toor"
        maxTotal="100"
        maxIdle="20"
        minIdle="5"
        maxWaitMillis="10000" />
```

### context.xml:
Bu bölümdeki ResourceLink, web uygulamasının kendi JNDI namespace’i içindeki jdbc/huseyinaydin adını, sunucuda server.xml içinde tanımlı olan global jdbc/huseyinaydin kaynağına sembolik olarak bağlar. Böylece uygulama, kendi context’inde sadece jdbc/huseyinaydin ismini kullanarak globalde tanımlanmış aynı DataSource’a erişebilir.
```xml
<ResourceLink
        name="jdbc/huseyinaydin"
        global="jdbc/huseyinaydin"
        auth="Container"
        type="javax.sql.DataSource" />
```

### Config:
Bu jee:jndi-lookup satırı, uygulama sunucusunda JNDI ile tanımlı jdbc/huseyinaydin isimli DataSource kaynağını bulup, Spring IoC konteyneri içinde dataSource id’li yonetimli bir bean olarak görünür hale getirir. Sonrasında bu dataSource bean’i, Spring’in Dependency Injection mekanizmasıyla ihtiyaç duyan repository ve servis sınıflarına enjekte edilerek kullanılır.
```xml
<jee:jndi-lookup
        id="dataSource"
        jndi-name="jdbc/huseyinaydin"
        expected-type="javax.sql.DataSource" />
```