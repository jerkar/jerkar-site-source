<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8"/>
    <title><#if (content.title)??><#escape x as x?xml>${content.title}</#escape><#else>Jerkar, the pure Java build tool</#if></title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="A powerfull pure Java build and automation tool">
    <meta name="author" content="Jerome Angibaud">
    <meta name="keywords" content="build,java,tool,automation,maven,ant,gradle,buildr,sbt,make,compiler,tester,jacoco,pure java build tool,java build tool,build tool,100% java build tool">
    <meta name="generator" content="JBake">

    <!-- Le styles -->
    <link href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>css/bootstrap.css" rel="stylesheet">
    <!-- <link href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>css/bootstrap.min.css" rel="stylesheet">  -->
    <link href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>css/asciidoctor.css" rel="stylesheet">
    <link href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>css/base.css" rel="stylesheet">
    <link href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>css/prettify.css" rel="stylesheet">
    <link href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>css/jerkar.css" rel="stylesheet">

    <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
      <script src="<#if (content.rootpath)??>${content.rootpath}<#else></#if>js/html5shiv.min.js"></script>
      
    <![endif]-->
    
      <!-- Fav and touch icons -->
    <!--<link rel="apple-touch-icon-precomposed" sizes="144x144" href="../assets/ico/apple-touch-icon-144-precomposed.png">
    <link rel="apple-touch-icon-precomposed" sizes="114x114" href="../assets/ico/apple-touch-icon-114-precomposed.png">
    <link rel="apple-touch-icon-precomposed" sizes="72x72" href="../assets/ico/apple-touch-icon-72-precomposed.png">
    <link rel="apple-touch-icon-precomposed" href="../assets/ico/apple-touch-icon-57-precomposed.png">-->
    <link rel="shortcut icon" href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>favicon.ico">
  
  	<!-- highlight.js -->
  	<script src="<#if (content.rootpath)??>${content.rootpath}<#else></#if>js/highlight.pack.js"></script>
	<script>hljs.initHighlightingOnLoad();</script>
	<link rel="stylesheet" href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>css/highlight-idea.css">
	
	<!-- sidebar css -->
	<link href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>css/sidebar.css" rel="stylesheet">
	
	<#if (content.addSideMenu)??>
		<link href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>css/numberify.css" rel="stylesheet">
	</#if>
  
  </head>
  <body onload="prettyPrint()">
    <div id="wrap">
   