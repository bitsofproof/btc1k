/*
 * Copyright 2013 bits of proof zrt.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bitsofproof.btc1k.server;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bitsofproof.supernode.common.ExtendedKey;
import com.bitsofproof.supernode.common.ValidationException;
import com.google.common.base.Preconditions;

public class EncryptedMasterInitializer
{
	private static final Logger log = LoggerFactory.getLogger (EncryptedMasterInitializer.class);

	private static final SecureRandom rnd = new SecureRandom ();

	private final String seedPath;

	private final Console console;

	public EncryptedMasterInitializer (String seedFilePath)
	{
		this.console = System.console ();
		this.seedPath = seedFilePath;
	}

	private void println (String msg)
	{
		if ( console == null )
		{
			System.out.println (msg);
		}
		else
		{
			console.writer ().println (msg);
		}
	}

	private String readPassphrase ()
	{
		if ( console == null )
		{
			System.out.print ("Enter passphrase for Master Key: ");
			try
			{
				return new BufferedReader (new InputStreamReader (System.in)).readLine ();
			}
			catch ( IOException e )
			{
				throw new RuntimeException (e);
			}
		}
		else
		{
			return String.copyValueOf (console.readPassword ("Enter passphrase for Master Key: "));
		}

	}

	public ExtendedKey initializeMasterKey ()
	{
		try
		{
			if ( seedPath.startsWith ("xprv") )
			{
				return ExtendedKey.parse (seedPath);
			}
			else if ( Files.exists (FileSystems.getDefault ().getPath (seedPath)) )
			{
				println ("Unlocking Master Key");
			}
			else
			{
				println ("No seed file exsists. Creating new Master Key");
			}

			System.out.print ("Enter passphrase for Master Key: ");
			String passphrase = readPassphrase ();
			return (!Files.exists (FileSystems.getDefault ().getPath (seedPath)))
					? generateNewKey (passphrase)
					: decryptMasterKey (passphrase);
		}
		catch ( Exception e )
		{
			println ("Error unlocking Master Key. " + e.getMessage ());
			System.exit (1);
			throw new IllegalStateException ("Error during generating master key");
		}
	}

	private ExtendedKey decryptMasterKey (String passphrase) throws ValidationException
	{
		int fingerprint;
		byte[] seed = new byte[32];

		log.info ("Reading encrypted seed from {}", seedPath);
		try ( ObjectInputStream ois = new ObjectInputStream (Files.newInputStream (FileSystems.getDefault ().getPath (seedPath), READ)) )
		{
			fingerprint = ois.readInt ();
			ois.readFully (seed);
		}
		catch ( IOException e )
		{
			throw new IllegalStateException ("Error reading encrypted seed");
		}

		ExtendedKey key = ExtendedKey.createFromPassphrase (passphrase, seed);

		Preconditions.checkState (fingerprint == key.getFingerPrint (), "Fingerprint doesn't match");

		return key;
	}

	private ExtendedKey generateNewKey (String passphrase) throws ValidationException
	{
		log.info ("Encrypted seed doesn't exists. Generating new key");

		byte[] seed = new byte[32];
		rnd.nextBytes (seed);

		ExtendedKey key = ExtendedKey.createFromPassphrase (passphrase, seed);

		log.info ("Writing out encrypted seed");
		try ( ObjectOutputStream oos = new ObjectOutputStream (
				Files.newOutputStream (FileSystems.getDefault ().getPath (seedPath), WRITE, TRUNCATE_EXISTING, CREATE)) )
		{
			oos.writeInt (key.getFingerPrint ());
			oos.write (seed);
		}
		catch ( IOException e )
		{
			throw new IllegalStateException ("Error writing encrypted seed");
		}

		return key;
	}
}
