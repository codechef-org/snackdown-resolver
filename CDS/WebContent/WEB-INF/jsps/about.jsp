<% request.setAttribute("title", "About"); %>
<%@ include file="layout/head.jsp" %>
<div class="container-fluid">
    <div class="row">
        <div class="col-8">
        <div class="card">
        <div class="card-body p-0">
            <p class="indent">The Contest Data Server provides secure, authenticated access to all contest data. It is primarily a Contest
            API server that proxies a Contest Control System and other contest services via standard HTTP requests, but it also has a web
            UI for contest information and administration.</p>

            <table class="table table-sm table-hover table-striped mb-4">
                <tbody>
                <tr>
                    <td>ICPC Tools website</td>
                    <td><a href="http://tools.icpc.global"
                           target="_blank">http://tools.icpc.global</a></td>
                </tr>
                <tr>
                    <td>ICPC Specifications</td>
                    <td><a href="https://ccs-specs.icpc.io"
                           target="_blank">https://ccs-specs.icpc.io</a></td>
                </tr>
                <tr>
                    <td>Github</td>
                    <td>
                        <a href="https://github.com/icpctools/icpctools" target="_blank">https://github.com/icpctools/icpctools</a>
                    </td>
                </tr>
                </tbody>
            </table>
            </div></div>
        </div>
        <div class="col-4">
          <img src="logo.png" height="240"/>
        </div>
    </div>
</div>
<%@ include file="layout/footer.jsp" %>