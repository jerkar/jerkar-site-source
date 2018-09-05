		</div>
		<div id="push"></div>
    </div>
    
    <div id="footer">
      <div class="container">
        <p class="muted credit">&copy; 2014 | Mixed with <a href="http://getbootstrap.com/">Bootstrap v3.1.1</a> | Baked with <a href="http://jbake.org">JBake ${version}</a></p>
      </div>
    </div>
    
   
    
    <!-- javascript =========================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script src="<#if (content.rootpath)??>${content.rootpath}<#else></#if>js/jquery-1.11.1.min.js"></script>
    <script src="<#if (content.rootpath)??>${content.rootpath}<#else></#if>js/bootstrap.min.js"></script>
    <script src="<#if (content.rootpath)??>${content.rootpath}<#else></#if>js/prettify.js"></script>
 	<script>
 	    lastSelect = null;
    	$('.liexpandable').click(function(evt) {
    		var li = $(evt.target).parents("li").first();
    		var ul = li.find(".sub-menu").first();
    		console.log(ul.is(':visible'));
    		evt.stopPropagation();
    		if (ul.is(':visible') && li.html() !== lastSelect) {
    			lastSelect = li.html();
    			return;
    		}
    		lastSelect = li.html();
      		ul.toggle();
 		});
    </script>
    
    <!-- Google Analytics -->
    <script>
      (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
      (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
      m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
      })(window,document,'script','https://www.google-analytics.com/analytics.js','ga');

      ga('create', 'UA-103449580-1', 'auto');
      ga('send', 'pageview');
    </script>
    
  </body>
</html>