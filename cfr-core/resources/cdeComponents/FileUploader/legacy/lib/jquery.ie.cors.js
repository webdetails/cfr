/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/
(function( jQuery ) {
  if ( window.XDomainRequest ) {
    jQuery.ajaxTransport(function( s ) {
      if ( s.crossDomain && s.async ) {
        if ( s.timeout ) {
          s.xdrTimeout = s.timeout;
          delete s.timeout;
        }
        var xdr;
        return {
          send: function( _, complete ) {
            function callback( status, statusText, responses, responseHeaders ) {
              xdr.onload = xdr.onerror = xdr.ontimeout = jQuery.noop;
              xdr = undefined;
              complete( status, statusText, responses, responseHeaders );
            }
            xdr = new window.XDomainRequest();
            xdr.onload = function() {
              callback( 200, "OK", { text: xdr.responseText }, "Content-Type: " + xdr.contentType );
            };
            xdr.onerror = function() {
              callback( 404, "Not Found" );
            };
            xdr.onprogress = function() {}; 
            if ( s.xdrTimeout ) {
              xdr.ontimeout = function() {
                callback( 0, "timeout" );
              };
              xdr.timeout = s.xdrTimeout;
            }

            xdr.open( s.type, s.url, true );
            xdr.send( ( s.hasContent && s.data ) || null );
          },
          abort: function() {
            if ( xdr ) {
              xdr.onerror = jQuery.noop();
              xdr.abort();
            }
          }
        };
      }
    });
  }
})( jQuery );