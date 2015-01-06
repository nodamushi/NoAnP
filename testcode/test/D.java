package test;

import nodamushi.annotation.SuppressCloneWarning;
import nodamushi.annotation.SuppressOverrideWarning;

/**
 * SuppressCloneWarning,SuppressOverrideWarning("abc")により
 * 警告が抑制されます。（外すと警告が出ます）
 * @author nodamushi
 *
 */
@SuppressCloneWarning
@SuppressOverrideWarning("abc")
public class D extends C{

}
