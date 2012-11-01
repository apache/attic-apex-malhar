/*
 *  Copyright (c) 2012 Malhar, Inc.
 *  All Rights Reserved.
 */
package com.malhartech.demos.twitter;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

import com.malhartech.annotation.ShipContainingJars;
import com.malhartech.api.ActivationListener;
import com.malhartech.api.Context.OperatorContext;
import com.malhartech.api.DefaultOutputPort;
import com.malhartech.api.InputOperator;
import com.malhartech.util.CircularBuffer;

/**
 *
 * @author Chetan Narsude <chetan@malhar-inc.com>
 */
@ShipContainingJars(classes = {StatusListener.class, Status.class})
public abstract class TwitterSampleInput implements InputOperator, ActivationListener<OperatorContext>, StatusListener
{
  private static final Logger logger = LoggerFactory.getLogger(TwitterSampleInput.class);
  public final transient DefaultOutputPort<Status> status = new DefaultOutputPort<Status>(this);
  public final transient DefaultOutputPort<String> text = new DefaultOutputPort<String>(this);
  public final transient DefaultOutputPort<String> url = new DefaultOutputPort<String>(this);
  public final transient DefaultOutputPort<?> userMention = null;
  public final transient DefaultOutputPort<?> hashtag = null;
  public final transient DefaultOutputPort<?> media = null;
  /**
   * For tapping into the tweets.
   */
  transient TwitterStream ts;
  transient CircularBuffer<Status> statuses = new CircularBuffer<Status>(1024 * 1024, 10);
  transient int count;
  /**
   * The state which we would like to save for this operator.
   */
  int multiplier;
  private Properties twitterProperties;

  @Override
  public void setup(OperatorContext context)
  {
    if (multiplier != 1) {
      logger.info("Load set to be {}% of the entire twitter feed", multiplier);
    }

    ConfigurationBuilder cb = new ConfigurationBuilder();
    cb.setDebugEnabled(Boolean.valueOf(twitterProperties.getProperty("debug"))).
            setOAuthConsumerKey(twitterProperties.getProperty("oauth.consumerKey")).
            setOAuthConsumerSecret(twitterProperties.getProperty("oauth.consumerSecret")).
            setOAuthAccessToken(twitterProperties.getProperty("oauth.accessToken")).
            setOAuthAccessTokenSecret(twitterProperties.getProperty("oauth.accessTokenSecret"));
    ts = new TwitterStreamFactory(cb.build()).getInstance();
  }

  @Override
  public void teardown()
  {
    ts = null;
  }

  @Override
  public void onStatus(Status status)
  {
    try {
      for (int i = multiplier; i-- > 0;) {
        statuses.put(status);
        count++;
      }
    }
    catch (InterruptedException ex) {
    }
  }

  @Override
  public void endWindow()
  {
    if (count % 16 == 0) {
      logger.debug("processed {} statuses", count);
    }
  }

  @Override
  public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice)
  {
    // do nothing
  }

  @Override
  public void onTrackLimitationNotice(int numberOfLimitedStatuses)
  {
    // do nothing
  }

  @Override
  public void onScrubGeo(long userId, long upToStatusId)
  {
    // do nothing
  }

  @Override
  public void onException(Exception excptn)
  {
    logger.info("Stopping samping because {}", excptn.getLocalizedMessage());
    synchronized (this) {
      this.notifyAll();
    }
  }

  @Override
  public void beginWindow()
  {
  }

  @Override
  public void postActivate(OperatorContext context)
  {
    ts.addListener(this);
    // we can only listen to tweets containing links by callng ts.links().
    // it seems it requires prior signed agreement with twitter.
    ts.sample();
  }

  @Override
  public void preDeactivate()
  {
    ts.shutdown();
  }

  @Override
  public void replayTuples(long arg0) {
  }

  void setTwitterProperties(Properties properties)
  {
    twitterProperties = properties;
  }

  void setFeedMultiplier(int multiplier)
  {
    this.multiplier = multiplier;
  }
}
