import { DOCUMENT } from '@angular/common';
import {
  AfterViewInit,
  Component,
  ElementRef,
  HostListener,
  Inject,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  SimpleChanges,
  ViewEncapsulation
} from '@angular/core';
import { Subscription } from 'rxjs';
import { NbThemeService, NbToastrService } from '@nebular/theme';

import {
  ActionZoomEvent,
  CANVAS_TRANSITION_TIMING,
  MAX_SCALE,
  MIN_SCALE,
  OffsetPosition,
  Position,
  ZoomValue,
  ZOOM_SCALE,
  ZOOM_SPEED
} from './models';

@Component({
  selector: '[canvas]',
  templateUrl: './canvas.component.html',
  styleUrls: ['./canvas.scss'],
  encapsulation: ViewEncapsulation.None
})
export class CanvasComponent implements OnInit, OnChanges, OnDestroy, AfterViewInit {
  @Input() position: OffsetPosition;
  @Input() centerCanvasAtInitialisation: boolean = true;
  @Input() fullscreenWrapper: 'current' | 'parent' = 'current';

  private canvasPos = { x: 0, y: 0 };
  private canvasPosOffset = { x: 0, y: 0 };
  private pointer = { x: 0, y: 0 };
  private canvasScale: number = MAX_SCALE;
  private isDragingCanvas: Position;
  isFullscreen: boolean = false;

  private wrapper: HTMLElement;
  canvas: HTMLElement;

  private subscription: Subscription = new Subscription();

  constructor(
    @Inject(DOCUMENT) private document: Document,
    private canvasWrapperElement: ElementRef,
    private themeService: NbThemeService,
    private toastrService: NbToastrService
  ) {}

  ngOnInit(): void {
    this.wrapper = this.canvasWrapperElement.nativeElement;
    this.wrapper.style.position = 'relative';
    this.wrapper.style.cursor = 'move';

    this.subscription = this.themeService.onThemeChange().subscribe((theme: any) => {
      if (theme.name === 'default') {
        if (this.fullscreenWrapper === 'current') this.wrapper.style.backgroundColor = '#edf1f7';
        else this.wrapper.parentElement.style.backgroundColor = '#edf1f7';
      } else if (theme.name === 'dark') {
        if (this.fullscreenWrapper === 'current') this.wrapper.style.backgroundColor = '#151a30';
        else this.wrapper.parentElement.style.backgroundColor = '#151a30';
      }
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (
      this.canvas &&
      changes.position.currentValue &&
      changes.position.currentValue !== changes.position.previousValue
    ) {
      this.centerOnElement(changes.position.currentValue);
    }
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      if (this.wrapper.children.length > 1) this.canvas = this.wrapper.querySelector('#canvas');
      else this.canvas = this.wrapper.firstElementChild as HTMLElement;

      if (this.canvas) {
        if (this.centerCanvasAtInitialisation) this.centerCanvas();
      } else {
        throw new Error(
          'The container contains several elements. It is necessary to give a "canvas" identifier to the canvas element'
        );
      }
    }, 0);
  }

  zoomCanvas(wheelEvent?: WheelEvent, actionEvent?: ActionZoomEvent): void {
    this.throwErrorUndefinedCanvas();
    if (!wheelEvent && !actionEvent) throw new Error('No event defined');

    this.pointer.x = (wheelEvent?.clientX || actionEvent.clientX) - this.wrapper.offsetLeft;
    this.pointer.y = (wheelEvent?.clientY || actionEvent.clientY) - this.wrapper.offsetTop;
    this.canvasPosOffset.x = (this.pointer.x - this.canvasPos.x) / this.canvasScale;
    this.canvasPosOffset.y = (this.pointer.y - this.canvasPos.y) / this.canvasScale;

    const scale = wheelEvent ? wheelEvent.deltaY : actionEvent.scale;
    this.canvasScale += -1 * Math.max(-1, Math.min(1, scale)) * ZOOM_SPEED * this.canvasScale;

    this.canvasScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, this.canvasScale));

    this.canvasPos.x = -this.canvasPosOffset.x * this.canvasScale + this.pointer.x;
    this.canvasPos.y = -this.canvasPosOffset.y * this.canvasScale + this.pointer.y;

    this.canvas.style.transform = `translate(${this.canvasPos.x}px,${this.canvasPos.y}px) scale(${this.canvasScale},${this.canvasScale})`;
  }

  fullscreen(open: boolean): void {
    if (open) {
      this.openFullscreen();
    } else {
      this.closeFullscreen();
    }
  }

  openFullscreen(): void {
    this.document.body.requestFullscreen();

    if (this.fullscreenWrapper === 'current') this.wrapper.classList.add('wrapperFullscreen');
    else this.wrapper.parentElement.classList.add('wrapperFullscreen');

    this.canvas.classList.add('canvasFullscreen');
  }

  closeFullscreen(): void {
    if (this.fullscreenWrapper === 'current') this.wrapper.classList.remove('wrapperFullscreen');
    else this.wrapper.parentElement.classList.remove('wrapperFullscreen');

    this.canvas.classList.remove('canvasFullscreen');

    if (this.document.fullscreenElement) {
      this.document.exitFullscreen();
    }
  }

  zoom(v: ZoomValue): void {
    const actionEvent: ActionZoomEvent = {
      scale: v === ZoomValue.OUT ? ZOOM_SCALE : -ZOOM_SCALE,
      clientX: this.wrapper.offsetWidth / 2 + this.wrapper.offsetLeft,
      clientY: this.wrapper.offsetHeight / 2 + this.wrapper.offsetTop
    };
    this.zoomCanvas(null, actionEvent);
  }

  centerCanvas(): void {
    this.throwErrorUndefinedCanvas();

    this.canvasPos.x =
      ((this.canvas.offsetWidth * this.canvasScale - this.wrapper.offsetWidth) / 2) * -1;
    this.canvasPos.y = 0;

    this.canvas.style.transform = `translate(${this.canvasPos.x}px,${this.canvasPos.y}px) scale(${this.canvasScale},${this.canvasScale})`;
  }

  centerOnElement(position: OffsetPosition): void {
    this.throwErrorUndefinedCanvas();

    this.canvasPos.x =
      position.offsetLeft * this.canvasScale * -1 +
      (this.wrapper.offsetWidth - position.offsetWidth) / 2;
    this.canvasPos.y =
      position.offsetTop * this.canvasScale * -1 +
      (this.wrapper.offsetHeight - position.offsetHeight) / 2;

    this.canvas.style.transform = `translate(${this.canvasPos.x}px,${this.canvasPos.y}px) scale(${this.canvasScale},${this.canvasScale})`;
  }

  throwErrorUndefinedCanvas(): void {
    if (!this.canvas) throw new Error('The canvas is not defined');
  }

  @HostListener('wheel', ['$event'])
  public onWheel(event: WheelEvent) {
    event.preventDefault();
    this.throwErrorUndefinedCanvas();

    this.zoomCanvas(event);
  }

  /**
   * Takes the new positions of the canvas and the client
   * @param {MouseEvent} event
   */
  @HostListener('mousedown', ['$event'])
  onMouseDownCanvas(event: MouseEvent): void {
    this.throwErrorUndefinedCanvas();

    if (event.button == 0) {
      this.isDragingCanvas = {
        left: this.canvasPos.x,
        top: this.canvasPos.y,
        clientX: event.clientX,
        clientY: event.clientY
      };
      let canvas = this.canvas;
      canvas.style.transition = 'unset';
    }
  }

  /**
   * Stop the slide of the canvas
   * @param {MouseEvent} event
   */
  @HostListener('mouseup', ['$event'])
  onMouseUpCanvas(event: MouseEvent): void {
    this.throwErrorUndefinedCanvas();

    if (event.button == 0) {
      this.isDragingCanvas = undefined;
      let canvas = this.canvas;
      canvas.style.transition = `transform .${CANVAS_TRANSITION_TIMING}s`;
    }
  }

  /**
   * Change the position of the canvas in the container
   * @param {MouseEvent} event
   */
  @HostListener('mousemove', ['$event'])
  onMouseMoveCanvas(event: MouseEvent): void {
    if (this.canvas && this.isDragingCanvas && event.button == 0) {
      let canvas = this.canvas;
      const dx = event.clientX - this.isDragingCanvas.clientX;
      const dy = event.clientY - this.isDragingCanvas.clientY;
      this.canvasPos.x = this.isDragingCanvas.left + dx;
      this.canvasPos.y = this.isDragingCanvas.top + dy;
      canvas.style.transform = `translate(${this.canvasPos.x}px,${this.canvasPos.y}px) scale(${this.canvasScale},${this.canvasScale})`;
    }
  }

  @HostListener('document:fullscreenchange', ['$event'])
  onFullscreenchange(): void {
    if (!this.document.fullscreenElement) {
      this.isFullscreen = false;
      this.closeFullscreen();
    } else {
      this.isFullscreen = true;
    }
  }

  @HostListener('document:fullscreenerror', ['$event'])
  onFullscreenerror(): void {
    this.toastrService.danger(`An error has occurred`, 'Error', {
      duration: 5000,
      status: 'danger'
    });
  }
}
