package com.studentprofiling.studentprofiling.ServiceClasses;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired private JavaMailSender mailSender;

    @Value("${mail.from:danvirtuous2004@gmail.com}")
    private String from;

    @Value("${mail.senderName:Assumption College of Davao}")
    private String senderName;

    @Value("${app.public-base-url:}")
    private String publicBaseUrl;

    private static String esc(String s) {
    if (s == null) return "";
    return s.replace("&","&amp;").replace("<","&lt;")
            .replace(">","&gt;").replace("\"","&quot;")
            .replace("'", "&#39;");
  }
    

    public boolean sendVerificationCode(String to, String code) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");

            helper.setTo(to);
            helper.setFrom(from, senderName);
            helper.setSubject("Assumption College of Davao: SPS Verification Code");

            
            String html =
                """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>SPS Verification Code</title>
                </head>
                <body style="margin:0;background:#f5f7fb;font-family:Arial,Helvetica,sans-serif;color:#0b2d4a;">
                  <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background:#f5f7fb;padding:24px 0;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="640" cellspacing="0" cellpadding="0" style="background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 8px 24px rgba(0,0,0,.06);">
                          <tr>
                            <td style="padding:0;">
                              <img alt="Assumption College of Davao" src="cid:acdHeader" width="640" style="display:block;width:100%;height:auto;">
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:28px 32px 8px;">
                              <h2 style="margin:0 0 6px;font-size:20px;font-weight:700;color:#0b2d4a;">Your Email Verification Code is:</h2>
                            </td>
                          </tr>
                          <tr>
                            <td align="center" style="padding:8px 32px 12px;">
                              <div style="font-size:56px;line-height:1;font-weight:800;letter-spacing:6px;color:#0b63b6;">{{CODE}}</div>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:4px 32px 28px;">
                              <p style="margin:0;font-size:14px;color:#334e68;">
                                If you did not request this code, please ignore this email.
                              </p>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:0 32px 28px;color:#7b8794;font-size:12px;">
                              Assumption College of Davao
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """;

            html = html.replace("{{CODE}}", code);

            helper.setText(html, true);

           
            ClassPathResource header = new ClassPathResource("static/acd-header.png");
            if (header.exists()) {
                helper.addInline("acdHeader", header);
            }

            mailSender.send(mime);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

private String safe(String s){ return s == null ? "" : s; }

    public boolean sendAchievementDecisionEmail(String to, String decision,
                                            String achTitle, String desc,
                                            String date, String proofPath ) {
    try {
        if (to == null || to.isBlank()) return false;

        MimeMessage mime = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");

        helper.setTo(to);
        helper.setFrom(from, senderName);
        helper.setSubject("Your Academic Achievement request is " + decision);

        String html = """
            <!doctype html>
            <html>
            <body style="margin:0;background:#f5f7fb;font-family:Arial,Helvetica,sans-serif;color:#0b2d4a">
              <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background:#f5f7fb;padding:24px 0">
                <tr><td align="center">
                  <table role="presentation" width="640" cellspacing="0" cellpadding="0"
                         style="background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 8px 24px rgba(0,0,0,.06)">
                    <tr>
                      <td style="padding:0">
                        <img alt="Assumption College of Davao" src="cid:acdHeader" width="640"
                             style="display:block;width:100%;height:auto">
                      </td>
                    </tr>
                    <tr><td style="padding:24px 32px 6px"><p>({{STATUS_TEXT}})</p></td></tr>
                    <tr><td style="padding:6px 32px"><b>Request Type:</b> Achievement</td></tr>
                    <tr><td style="padding:6px 32px"><b>Achievement Type:</b> {{ACHIEVEMENT}}</td></tr>
                    <tr><td style="padding:6px 32px"><b>Description:</b> {{DESC}}</td></tr>
                    <tr><td style="padding:6px 32px"><b>Date:</b> {{DATE}}</td></tr>
                    <!-- Proof section intentionally removed -->
                    <tr>
                      <td style="padding:0 32px 28px;color:#7b8794;font-size:12px">
                        If you have any concerns for this decision, kindly approach your Program Head/Dean/Secretary at the Faculty Office.
                      </td>
                    </tr>
                  </table>
                </td></tr>
              </table>
            </body>
            </html>
        """;

        html = html
            .replace("{{STATUS_TEXT}}",
                     "ACCEPTED".equalsIgnoreCase(decision) ? "Your request is Accepted" : "Your request is Rejected")
            .replace("{{ACHIEVEMENT}}", safe(achTitle))
            .replace("{{DESC}}",        safe(desc))
            .replace("{{DATE}}",        safe(date));

        helper.setText(html, true);

        
        ClassPathResource header = new ClassPathResource("static/acd-header.png");
        if (header.exists()) helper.addInline("acdHeader", header);

        mailSender.send(mime);
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}

public boolean sendAuthorshipDecisionEmail(String to, String decision,
                                           String type, String desc,
                                           String date, String proofPath /* ignored */) {
    try {
        if (to == null || to.isBlank()) return false;

        MimeMessage mime = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");

        helper.setTo(to);
        helper.setFrom(from, senderName);
        helper.setSubject("Your Academic Authorship request is " + decision);

        String html = """
            <!doctype html>
            <html>
            <body style="margin:0;background:#f5f7fb;font-family:Arial,Helvetica,sans-serif;color:#0b2d4a">
              <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background:#f5f7fb;padding:24px 0">
                <tr><td align="center">
                  <table role="presentation" width="640" cellspacing="0" cellpadding="0"
                         style="background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 8px 24px rgba(0,0,0,.06)">
                    <tr>
                      <td style="padding:0">
                        <img alt="Assumption College of Davao" src="cid:acdHeader" width="640"
                             style="display:block;width:100%;height:auto">
                      </td>
                    </tr>
                    <tr><td style="padding:24px 32px 6px"><p>({{STATUS_TEXT}})</p></td></tr>
                    <tr><td style="padding:6px 32px"><b>Request Type:</b> Authorship</td></tr>
                    <tr><td style="padding:6px 32px"><b>Authorship Type:</b> {{TYPE}}</td></tr>
                    <tr><td style="padding:6px 32px"><b>Description:</b> {{DESC}}</td></tr>
                    <tr><td style="padding:6px 32px"><b>Date:</b> {{DATE}}</td></tr>
                    <!-- Proof section intentionally removed -->
                    <tr>
                      <td style="padding:0 32px 28px;color:#7b8794;font-size:12px">
                        If you have any concerns for this decision, kindly approach your Program Head/Dean/Secretary at the Faculty Office.
                      </td>
                    </tr>
                  </table>
                </td></tr>
              </table>
            </body>
            </html>
        """;

        html = html
            .replace("{{STATUS_TEXT}}",
                     "ACCEPTED".equalsIgnoreCase(decision) ? "Your request is Accepted" : "Your request is Rejected")
            .replace("{{TYPE}}", safe(type))
            .replace("{{DESC}}", safe(desc))
            .replace("{{DATE}}", safe(date));

        helper.setText(html, true);

        ClassPathResource header = new ClassPathResource("static/acd-header.png");
        if (header.exists()) helper.addInline("acdHeader", header);

        mailSender.send(mime);
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}

public boolean sendAchievementDeletionEmail(String to, String achTitle,
                                            String desc, String date, String reason) {
    try {
        if (to == null || to.isBlank()) return false;
        MimeMessage mime = mailSender.createMimeMessage();
        MimeMessageHelper h = new MimeMessageHelper(mime, true, "UTF-8");
        h.setTo(to);
        h.setFrom(from, senderName);
        h.setSubject("One of your Academic Information is now deleted.");

        String html = """
           <!doctype html><html><body style="margin:0;background:#f5f7fb;font-family:Arial,Helvetica,sans-serif;color:#0b2d4a">
             <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background:#f5f7fb;padding:24px 0">
               <tr><td align="center">
                 <table role="presentation" width="640" cellspacing="0" cellpadding="0"
                        style="background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 8px 24px rgba(0,0,0,.06)">
                   <tr><td style="padding:0"><img alt="Assumption College of Davao" src="cid:acdHeader" width="640" style="display:block;width:100%;height:auto"></td></tr>
                   <tr><td style="padding:24px 32px 6px"><h3>One of your Academic Information is now deleted.</h3></td></tr>
                   <tr><td style="padding:6px 32px"><b>Academic Information Type:</b> Achievement</td></tr>
                   <tr><td style="padding:6px 32px"><b>Achievement Type:</b> {{ACH}}</td></tr>
                   <tr><td style="padding:6px 32px"><b>Description:</b> {{DESC}}</td></tr>
                   <tr><td style="padding:6px 32px"><b>Date:</b> {{DATE}}</td></tr>
                   <tr><td style="padding:6px 32px"><b>Reason of Deletion:</b> {{REASON}}</td></tr>
                   <tr><td style="padding:0 32px 28px;color:#7b8794;font-size:12px">
                     If you have any concerns for this decision, kindly approach your Program Head/Dean/Secretary at the Faculty Office.
                   </td></tr>
                 </table>
               </td></tr>
             </table>
           </body></html>
        """;
        html = html.replace("{{ACH}}", esc(achTitle))
                   .replace("{{DESC}}", esc(desc))
                   .replace("{{DATE}}", esc(date))
                   .replace("{{REASON}}", esc(reason));

        h.setText(html, true);
        ClassPathResource header = new ClassPathResource("static/acd-header.png");
        if (header.exists()) h.addInline("acdHeader", header);
        mailSender.send(mime);
        return true;
    } catch(Exception e){ e.printStackTrace(); return false; }
}

public boolean sendAuthorshipDeletionEmail(String to, String type,
                                           String desc, String date, String reason) {
    try {
        if (to == null || to.isBlank()) return false;
        MimeMessage mime = mailSender.createMimeMessage();
        MimeMessageHelper h = new MimeMessageHelper(mime, true, "UTF-8");
        h.setTo(to);
        h.setFrom(from, senderName);
        h.setSubject("One of your Academic Information is now deleted.");

        String html = """
           <!doctype html><html><body style="margin:0;background:#f5f7fb;font-family:Arial,Helvetica,sans-serif;color:#0b2d4a">
             <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background:#f5f7fb;padding:24px 0">
               <tr><td align="center">
                 <table role="presentation" width="640" cellspacing="0" cellpadding="0"
                        style="background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 8px 24px rgba(0,0,0,.06)">
                   <tr><td style="padding:0"><img alt="Assumption College of Davao" src="cid:acdHeader" width="640" style="display:block;width:100%;height:auto"></td></tr>
                   <tr><td style="padding:24px 32px 6px"><h3>One of your Academic Information is now deleted.</h3></td></tr>
                   <tr><td style="padding:6px 32px"><b>Academic Information Type:</b> Achievement</td></tr>
                   <tr><td style="padding:6px 32px"><b>Authorship Type:</b> {{TYPE}}</td></tr>
                   <tr><td style="padding:6px 32px"><b>Description:</b> {{DESC}}</td></tr>
                   <tr><td style="padding:6px 32px"><b>Date:</b> {{DATE}}</td></tr>
                   <tr><td style="padding:6px 32px"><b>Reason of Deletion:</b> {{REASON}}</td></tr>
                   <tr><td style="padding:0 32px 28px;color:#7b8794;font-size:12px">
                     If you have any concerns for this decision, kindly approach your Program Head/Dean/Secretary at the Faculty Office.
                   </td></tr>
                 </table>
               </td></tr>
             </table>
           </body></html>
        """;
        html = html.replace("{{TYPE}}", esc(type))
                   .replace("{{DESC}}", esc(desc))
                   .replace("{{DATE}}", esc(date))
                   .replace("{{REASON}}", esc(reason));

        h.setText(html, true);
        ClassPathResource header = new ClassPathResource("static/acd-header.png");
        if (header.exists()) h.addInline("acdHeader", header);
        mailSender.send(mime);
        return true;
    } catch(Exception e){ e.printStackTrace(); return false; }
}



  }
