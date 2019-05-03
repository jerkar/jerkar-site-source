	<!-- Fixed navbar -->
    <div class="navbar navbar-default navbar-fixed-top" role="navigation">
      <div class="container">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand">
          	<img style="max-width:80px;margin-top: -10px;" src="<#if (content.rootpath)??>${content.rootpath}<#else></#if>img/logo/PNG-04.png">
          </a>
        </div>
        <div class="navbar-collapse collapse">
          <ul class="nav navbar-nav">
            <li><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>index.html">Home</a></li>
            <li class="dropdown">
              <a href="#" class="dropdown-toggle" data-toggle="dropdown">Documentation<b class="caret"></b></a>
              <ul class="dropdown-menu">
                <li><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>getting_started.html">Getting Started</a></li>
                <li><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>reference.html">Reference Guide</a></li>
                <li><a href="https://github.com/jerkar/jerkar-examples">Examples</a></li>
                <li><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>faq.html">Frequently Asked Questions</a></li>
                <li><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>javadoc/index.html">Javadoc</a></li>
              </ul>
            </li>
            <li><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>about.html">About</a></li>
          </ul>
        </div>
       
        
      </div>
    </div>
    <div class="container">